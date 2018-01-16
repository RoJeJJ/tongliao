import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.ISFSExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TLServlet extends HttpServlet {
    private ISFSExtension ex;
    @Override
    public void init() throws ServletException {
        ex = SmartFoxServer.getInstance().getZoneManager().getZoneByName("serverZone").getExtension();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestUri = req.getRequestURI();
        if ("/tongliao/halt".equals(requestUri)){
            ex.handleInternalMessage("halt",null);
        }else if ("/tongliao/user_card".equals(requestUri)){
            int uid = Integer.parseInt(req.getParameter("uid"));
            long card = Long.valueOf(req.getParameter("card"));
            ISFSObject object = new SFSObject();
            object.putInt("uid",uid);
            object.putLong("card",card);
            ex.handleInternalMessage("card",object);
        }else if ("/tongliao/vip".equals(requestUri)){
            int uid = Integer.parseInt(req.getParameter("uid"));
            boolean vip = Boolean.parseBoolean(req.getParameter("vip"));
            ISFSObject object = new SFSObject();
            object.putInt("uid",uid);
            object.putBool("vip",vip);
            ex.handleInternalMessage("vip",object);
        }
    }
}
