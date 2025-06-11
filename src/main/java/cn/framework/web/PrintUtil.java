package wameeee.framework.web;


import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintUtil {
    public static void write(Object obj, HttpServletResponse response)throws IOException{
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String json = JSONObject.toJSONString(obj);
        out.print(json);
        out.flush();
        out.close();
    }
}
