package server;

/**
 * @author mh_liu
 * @create 2018/10/19 上午11:14
 */
public class ResponseHeaders extends Headers {

    private int code;

    private String phrase;

    private String contentType;

    private String charset;

    private int contentLength;

    private String server;

    public ResponseHeaders(int code) {
        this.code = code;
        this.server = HttpConstant.SERVER_NAME;
        this.phrase = StatusCodeEnum.queryPhrase(code);
        this.charset = "UTF-8";
        setVersion(HttpConstant.DEFAULT_HTTP_VERSION);
    }


    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %d %s\r\n", getVersion(), code, phrase));
        sb.append(String.format("ContentType: %s;charset=%s\r\n", contentType,charset));
        sb.append(String.format("ContentLength: %d\r\n", contentLength));
        sb.append(String.format("Server: %s\r\n", server));
        sb.append("\r\n");
        return sb.toString();

    }
}
