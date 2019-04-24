package com.taobao.yugong.common.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * 用于信任任何证书链的策略,用于信任自签名https
 * @author chaijunkun
 * @since 2015年4月16日
 */
class AnyTrustStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return true;
    }

}

/**
 * 能访问自签名https站点的组件,支持断点续传,支持重试,支持上传文件
 * @author chaijunkun
 * @since 2015年4月16日
 */
public class HttpUtil {

    private static volatile HttpUtil instance;

    private ConnectionConfig connConfig;

    private SocketConfig socketConfig;

    private ConnectionSocketFactory plainSF;

    private KeyStore trustStore;

    private SSLContext sslContext;

    private LayeredConnectionSocketFactory sslSF;

    private Registry<ConnectionSocketFactory> registry;

    private PoolingHttpClientConnectionManager connManager;

    private volatile HttpClient client;

    private volatile BasicCookieStore cookieStore;

    public static final String DEFAULT_ENCODING = "utf-8";

    private static List<NameValuePair> paramsConverter(Map<String, String> params){
        List<NameValuePair> nvps = new LinkedList<NameValuePair>();
        Set<Map.Entry<String, String>> paramsSet = params.entrySet();
        for (Map.Entry<String, String> paramEntry : paramsSet) {
            nvps.add(new BasicNameValuePair(paramEntry.getKey(), paramEntry.getValue()));
        }
        return nvps;
    }

    private HttpUtil(){
        //设置连接参数
        connConfig = ConnectionConfig.custom().setCharset(Charset.forName(DEFAULT_ENCODING)).build();
        socketConfig = SocketConfig.custom().setSoTimeout(100000).build();
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
        plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);
        //指定信任密钥存储对象和连接套接字工厂
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, new AnyTrustStrategy()).build();
            sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registryBuilder.register("https", sslSF);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        registry = registryBuilder.build();
        //设置连接管理器
        connManager = new PoolingHttpClientConnectionManager(registry);
        connManager.setDefaultConnectionConfig(connConfig);
        connManager.setDefaultSocketConfig(socketConfig);
        //以下两个参数对并发影响很大，根据业务场景配置
        connManager.setMaxTotal(500);
        connManager.setDefaultMaxPerRoute(50);
        //指定cookie存储对象
        cookieStore = new BasicCookieStore();
        //构建客户端
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setConnectionManager(connManager).build();
    }

    public static HttpUtil getInstance(){
        synchronized (HttpUtil.class) {
            if (null == HttpUtil.instance){
                instance = new HttpUtil();
            }
            return instance;
        }
    }

    /**
     * 基本的Get请求
     * @param url 请求url
     * @param queryParams 请求头的查询参数
     * @return
     * @throws URISyntaxException
     */
    public HttpResponse doGet(String url, Map<String, String> queryParams) throws URISyntaxException, IOException {
        HttpGet gm = new HttpGet();
        URIBuilder builder = new URIBuilder(url);
        //填入查询参数
        if (MapUtils.isNotEmpty(queryParams)){
            builder.setParameters(HttpUtil.paramsConverter(queryParams));
        }
        gm.setURI(builder.build());
        HttpResponse response = client.execute(gm);
        if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
            if (HttpStatus.SC_NOT_FOUND == response.getStatusLine().getStatusCode()) {
                throw new FileNotFoundException("No Such File");
            } else {
                throw new IOException("Return Code Is Not OK");
            }
        }
        return response;
    }

    public HttpResponse doGet(String url) throws URISyntaxException, IOException {
        return doGet(url, null);
    }

    public String doGetRetString(String url, Map<String, String> queryParams, String encoding) throws URISyntaxException, IOException {
        HttpResponse response = this.doGet(url, queryParams);
        return EntityUtils.toString(response.getEntity(), HttpUtil.DEFAULT_ENCODING);
    }

    public String doGetRetString(String url, Map<String, String> queryParams) throws URISyntaxException, IOException {
        return this.doGetRetString(url, queryParams, null);
    }

    public String doGetRetString(String url) throws URISyntaxException, IOException {
        return this.doGetRetString(url, null, null);
    }

    /**
     * 基本Post请求
     * @param url 请求url
     * @param queryParams 请求头的查询参数
     * @param formParams post表单的参数
     * @return
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    public HttpResponse doPost(String url, Map<String, String> queryParams, Map<String, String> formParams) throws URISyntaxException, ClientProtocolException, IOException {
        HttpPost pm = new HttpPost();
        URIBuilder builder = new URIBuilder(url);
        //填入查询参数
        if (MapUtils.isNotEmpty(queryParams)){
            builder.setParameters(HttpUtil.paramsConverter(queryParams));
        }
        pm.setURI(builder.build());
        //填入表单参数
        if (MapUtils.isNotEmpty(formParams)){
            pm.setEntity(new UrlEncodedFormEntity(HttpUtil.paramsConverter(formParams), CharsetUtils.get(DEFAULT_ENCODING)));
        }

        HttpResponse response = client.execute(pm);
        if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
            if (HttpStatus.SC_NOT_FOUND == response.getStatusLine().getStatusCode()) {
                throw new FileNotFoundException("No Such File");
            } else {
                throw new IOException("Return Code Is Not OK");
            }
        }
        return response;
    }

    public HttpResponse doPost(String url, Map<String, String> queryParams) throws URISyntaxException, ClientProtocolException, IOException {
        return this.doPost(url, queryParams, null);
    }

    public HttpResponse doPost(String url) throws URISyntaxException, ClientProtocolException, IOException {
        return this.doPost(url, null, null);
    }

    public String doPostRetString(String url, Map<String, String> queryParams, Map<String, String> formParams, String encoding) throws URISyntaxException, ClientProtocolException, IOException {
        HttpResponse response = this.doPost(url, queryParams, formParams);
        return EntityUtils.toString(response.getEntity(), HttpUtil.DEFAULT_ENCODING);
    }

    public String doPostRetString(String url, Map<String, String> queryParams, Map<String, String> formParams) throws URISyntaxException, ClientProtocolException, IOException {
        return this.doPostRetString(url, queryParams, formParams, null);
    }

    public String doPostRetString(String url, Map<String, String> queryParams) throws URISyntaxException, ClientProtocolException, IOException {
        return this.doPostRetString(url, queryParams, null);
    }

    public String doPostRetString(String url) throws URISyntaxException, ClientProtocolException, IOException {
        return this.doPostRetString(url, null);
    }

    /**
     * 多块Post请求
     * @param url 请求url
     * @param queryParams 请求头的查询参数
     * @param formParts post表单的参数,支持字符串-文件(FilePart)和字符串-字符串(StringPart)形式的参数
     * @return
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public HttpResponse doMultipartPost(String url, Map<String, String> queryParams, List<FormBodyPart> formParts) throws URISyntaxException, ClientProtocolException, IOException {
        HttpPost pm = new HttpPost();
        URIBuilder builder = new URIBuilder(url);
        //填入查询参数
        if (MapUtils.isNotEmpty(queryParams)){
            builder.setParameters(HttpUtil.paramsConverter(queryParams));
        }
        pm.setURI(builder.build());
        //填入表单参数
        if (CollectionUtils.isNotEmpty(formParts)){
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setCharset(CharsetUtils.get(DEFAULT_ENCODING));
            entityBuilder = entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (FormBodyPart formPart : formParts) {
                entityBuilder = entityBuilder.addPart(formPart.getName(), formPart.getBody());
            }
            pm.setEntity(entityBuilder.build());
        }
        return client.execute(pm);
    }

    /**
     * 获取当前Http客户端状态中的Cookie
     * @param domain 作用域
     * @param port 端口 传null 默认80
     * @param path Cookie路径 传null 默认"/"
     * @param useSecure Cookie是否采用安全机制 传null 默认false
     * @return
     */
    public Map<String, Cookie> getCookie(String domain, Integer port, String path, Boolean useSecure){
        synchronized (cookieStore) {
            if (StringUtils.isBlank(domain)){
                return null;
            }
            if (null == port){
                port = 80;
            }
            if (StringUtils.isBlank(path)){
                path ="/";
            }
            if (null == useSecure){
                useSecure = false;
            }
            List<Cookie> cookies = cookieStore.getCookies();
            if (CollectionUtils.isEmpty(cookies)){
                return null;
            }

            CookieOrigin origin = new CookieOrigin(domain, port, path, useSecure);
            BestMatchSpec cookieSpec = new BestMatchSpec();
            Map<String, Cookie> retVal = new HashMap<String, Cookie>();
            for (Cookie cookie : cookies) {
                if(cookieSpec.match(cookie, origin)){
                    retVal.put(cookie.getName(), cookie);
                }
            }
            return retVal;
        }
    }

    /**
     * 批量设置Cookie
     * @param cookies cookie键值对图
     * @param domain 作用域 不可为空
     * @param path 路径 传null默认为"/"
     * @param useSecure 是否使用安全机制 传null 默认为false
     * @return 是否成功设置cookie
     */
    public boolean setCookie(Map<String, String> cookies, String domain, String path, Boolean useSecure){
        synchronized (cookieStore) {
            if (StringUtils.isBlank(domain)){
                return false;
            }
            if (StringUtils.isBlank(path)){
                path = "/";
            }
            if (null == useSecure){
                useSecure = false;
            }
            if (MapUtils.isEmpty(cookies)){
                return true;
            }
            Set<Map.Entry<String, String>> set = cookies.entrySet();
            String key = null;
            String value = null;
            for (Map.Entry<String, String> entry : set) {
                key = entry.getKey();
                if (StringUtils.isBlank(key)){
                    return false;
                }
                value = entry.getValue();
                if (StringUtils.isBlank(value)){
                    return false;
                }
                BasicClientCookie cookie = new BasicClientCookie(key, value);
                cookie.setDomain(domain);
                cookie.setPath(path);
                cookie.setSecure(useSecure);
                cookieStore.addCookie(cookie);
            }
            return true;
        }

    }

    /**
     * 设置单个Cookie
     * @param key Cookie键
     * @param value Cookie值
     * @param domain 作用域 不可为空
     * @param path 路径 传null默认为"/"
     * @param useSecure 是否使用安全机制 传null 默认为false
     * @return 是否成功设置cookie
     */
    public boolean setCookie(String key, String value, String domain, String path, Boolean useSecure){
        Map<String, String> cookies = new HashMap<String, String>();
        cookies.put(key, value);
        return setCookie(cookies, domain, path, useSecure);
    }

}