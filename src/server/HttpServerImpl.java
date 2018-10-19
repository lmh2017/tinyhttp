package server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author mh_liu
 * @create 2018/10/18 下午6:12
 */
public class HttpServerImpl implements HttpServer{

    private int port;

    private static int BUFFER_SIZE= 1024;

    private ServerSocketChannel ssc;

    private Selector selector;

    private final static String DEFAULT_SOURCE_DIR = "static";

    private final String localCharset = "UTF-8";//字符编码

    public HttpServerImpl(int port) throws IOException {
        this.port = port;
        ssc = ServerSocketChannel.open();
        selector = Selector.open();

    }

    @Override
    public void server() {
        //绑定ip和端口号
        try {
            ssc.bind(new InetSocketAddress(port));
            ssc.configureBlocking(false);
            System.out.println(String.format("TinyHttpd 已启动，正在监听 %d 端口...", port));
            ssc.register(selector,SelectionKey.OP_ACCEPT);

            while(true){
                listen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            int num = selector.select();  //调用select 函数  ，底层实现epoll_ctl和epoll_wait函数，监听epoll实例
            if(num ==0){
                return;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while(it.hasNext()){
                SelectionKey key = it.next();
                it.remove();
                if(key.isAcceptable()){
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    sc.register(selector,SelectionKey.OP_READ);
                }else if(key.isReadable()){
                    request(key);
                    key.interestOps(SelectionKey.OP_WRITE);
                }else if(key.isWritable()){
                    response(key);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void request(SelectionKey sk) throws IOException {
        SocketChannel channel = (SocketChannel) sk.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        channel.read(buffer);
        buffer.flip();

        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        try {
            Headers headers = parseHeader(bytes);
            sk.attach(Optional.of(headers));
        }catch (ParseHeaderException e){
            sk.attach(Optional.empty());
        }

    }

    private Headers parseHeader(byte[] bytes) {
        if(bytes.length == 0){
            throw new ParseHeaderException("请求报文头内容为空");
        }
        Headers headers = new Headers();
        String requestHeader = new String(bytes);

        String[] headerStr = requestHeader.split("\r\n");

        String headerHead = headerStr[0];

        String[] firstLine = headerHead.split(" ");
        if(firstLine.length<3){
            throw new ParseHeaderException("请求报文头格式不符");
        }

        headers.setMethod(firstLine[0]);
        headers.setPath(firstLine[1]);
        headers.setVersion(firstLine[2]);

        for(int i=1;i<headerStr.length;i++){
            String headDeatil = headerStr[i];
            int index = headDeatil.indexOf(":");
            if(index == -1){
                continue;
            }
            String key = headDeatil.substring(0,index);
            if(index == -1 || index+1>headDeatil.length()){
                headers.set(key,"");
                continue;
            }
            String value = headDeatil.substring(index+1);
            headers.set(key,value);
        }
        return headers;

    }

    public void response(SelectionKey sk) throws IOException {
        SocketChannel channel = (SocketChannel) sk.channel();
        Optional<Headers> op = (Optional<Headers>) sk.attachment();
        if(!op.isPresent()){
            channel.close();
            return;
        }
        Charset latin1 = Charset.forName( localCharset );


        String remotHost = channel.getRemoteAddress().toString();
        int index = remotHost.indexOf("/");
        String ip = remotHost.substring(index+1);
        String[] ips = ip.split(":");
        System.out.println("请求地址为："+ips[0]);

        ByteBuffer buffer;
        if(!ips[0].equals("127.0.0.1")){
            String responseMsg = buildResponse(403,"拒绝访问","来自ip："+ips[0]+"的用户您好，服务器拒绝您的访问，若需访问请联系管理员，添加ip白名单");
            buffer = ByteBuffer.wrap(responseMsg.getBytes(localCharset));
            channel.write(buffer);
            channel.close();
            return;
        }

        if(!foundPath(op.get())){
            String responseMsg = buildResponse(404,"找不到服务器","来自ip："+ips[0]+"的用户您好，找不到目标服务地址");
            buffer = ByteBuffer.wrap(responseMsg.getBytes(localCharset));
            channel.write(buffer);
            channel.close();
            return;
        }

        String responseMsg = buildResponse(200,"显示页面","来自ip："+ips[0]+"的用户您好，欢迎访问小型http服务器，tinyhttp");
        buffer = ByteBuffer.wrap(responseMsg.getBytes(localCharset));
        channel.write(buffer);
        channel.close();
    }

    private boolean foundPath(Headers headers){
        String path = headers.getPath();
        int index = path.indexOf("/");
        path = path.substring(index);

        File file = new File(DEFAULT_SOURCE_DIR);
        String[] fileList = file.list();

        for(String name:fileList){
            if(name.equals(path)){
                return true;
            }
        }

        return false;
    }

    private String buildResponse(int code,String title,String context){
        ResponseHeaders headers = new ResponseHeaders(code);
        headers.setContentType("text/html");

        ResponseBody responseBody = new ResponseBody();
        responseBody.setTitle(title);
        responseBody.setContext(context);

         return headers.toString()+responseBody.toString();
    }

    public static void main(String[] args){
        try {
            HttpServer httpServer = new HttpServerImpl(10000);
            httpServer.server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
