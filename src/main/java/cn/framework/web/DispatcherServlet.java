package wameeee.framework.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = -1739193990812222775L;
    protected Map<String, Object> controllers = new HashMap<String, Object>();
    protected Map<String, Method> methods = new HashMap<String, Method>();
    private String webPackage = "wameeee.framework.web";
    private ControllerMappingManager ctrlMappingMgr;

    public void init() throws ServletException {
        String webPackage = getServletConfig().getInitParameter("webPackage");
        if (!(webPackage == null || (webPackage = webPackage.trim()).equals("")))
            this.webPackage = webPackage;
        ctrlMappingMgr = new ControllerMappingManager(this.webPackage);

        synchronized (controllers) {
            controllers.clear();
        }
        synchronized (methods) {
            methods.clear();
        }
    }

    public void destroy() {
        super.destroy();
        synchronized (controllers) {
            controllers.clear();
        }
        synchronized (methods) {
            methods.clear();
        }
        this.ctrlMappingMgr.getControllerMappings().clear();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        execute(request, response);
    }

    protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = null;
        path = (String) request.getAttribute("javax.servlet.include.pathInfo");
        if (path != null) {
            path = request.getPathInfo();
            if (path == null || (path = path.trim()).isEmpty()) {
                path = (String) request.getAttribute("javax.servlet.include.servlet_path");
                if (path == null) {
                    path = request.getServletPath();
                }
                int slash = path.lastIndexOf("/");
                int period = path.lastIndexOf(".");
                if ((period >= 0) && (period > slash)) {
                    path = path.substring(0, period);
                }
            }
        }
        System.out.println("===============Path:" + path);

        if (!ctrlMappingMgr.containsKey(path))
            throw new PathException("No Controller & Method is mapped with this path:" + path);
        ControllerMapping controllerMapping = ctrlMappingMgr.getControllerMapping(path);
        try {
            Object instance = null;
            synchronized (controllers) {
                instance = controllers.get(controllerMapping.getClassName());
                if (instance == null) {
                    instance = Class.forName(controllerMapping.getClassName()).newInstance();
                    controllers.put(controllerMapping.getClassName(), instance);
                }
            }
            Method method = null;
            synchronized (methods) {
                method = methods.get(path);
                if (method == null) {
                    method = instance.getClass().getMethod(controllerMapping.getMethodName(), HttpServletRequest.class, HttpServletResponse.class);
                    methods.put(path, method);
                }
            }
            Object result = method.invoke(instance, request, response);
            toView(result, request, response);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected void toView(Object result, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (result == null)
            return;
        if (result instanceof String) {
            boolean isRedirect = false;
            String url = (String) result;
            if (url.startsWith("redirect:")) {
                isRedirect = true;
                url = url.substring("redirect:".length());
            } else if (url.startsWith("forward:")) {
                url = url.substring("forward:".length());
            }
            if (!(url = url.trim()).startsWith("/"))
                throw new ViewPathException();
            if (isRedirect)
                response.sendRedirect(request.getContextPath() + url);
            else
                request.getRequestDispatcher(url).forward(request, response);
        } else {
            PrintUtil.write(result, response);
        }
    }
}
