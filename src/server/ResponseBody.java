package server;

/**
 * @author mh_liu
 * @create 2018/10/19 下午5:47
 */
public class ResponseBody {

    private String title;

    private String context;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String toString(){
        return "<html>" +
                "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head>" +
                "<title>"+title+"</title>" +
                "<body>" +
                context +
                "</body>" +
                "</html>";
    }
}
