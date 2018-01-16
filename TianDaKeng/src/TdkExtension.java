
import sfs2x.master.Ibase.IExtension;
import sfs2x.master.Ibase.ISeat;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkTable;

public class TdkExtension extends IExtension {
    private TdkTable table;
    @Override
    public void init() {
        table = (TdkTable) super.table;
    }

    @Override
    protected void reconnect(ISeat seat) {

    }

    @Override
    public Object handleInternalMessage(String cmdName, Object params) {
        switch (cmdName){
            case "jr":
                Player p = (Player) params;
                break;
        }
        return null;
    }

    public void joinRequest(Player p){

    }
}
