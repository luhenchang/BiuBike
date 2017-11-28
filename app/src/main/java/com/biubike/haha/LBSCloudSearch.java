package com.biubike.haha;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.biubike.LBSActivity;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class LBSCloudSearch {
	
	private static String mTAG = "NetWorkManager";
	//百度云检索API URI
	private static final String SEARCH_URI_NEARBY = "http://api.map.baidu.com/geosearch/v2/nearby?";
	private static final String SEARCH_URI_LOCAL = "http://api.map.baidu.com/geosearch/v2/local?";
	
	public static final int SEARCH_TYPE_NEARBY = 1;
	public static final int SEARCH_TYPE_LOCAL = 2;
	
	private static int currSearchType = 0;

	//云检索公钥：这个·东西必须要以"Server"去获取ak。不能通过android来获取appKey:我发现这两个东西居然是没关系的。
	// 只需要你的应用名称一样就可以了：但是你必须要设置半斤和自己的坐标。记住了必须自己要设置好百度lbs数据表要同意同步到云端ok？
	private static String ak ="rjOY9xDVgrxTG5UtDlkfHgOEYhwBiv9x";
	//private static String mcode="82:19:EF:6E:3E:D8:C5:37:16:3A:C1:53:A4:DA:25:17:61:A1:C2:52;com.biubike";
	private static String geotable_id ="179931";
	private static int TIME_OUT = 12000;
	private static int retry = 3;
	private static boolean IsBusy = false;

	/**
	 * 云检索访问
	 * @param filterParams	访问参数，key为filter时特殊处理。
	 * @param handler		数据回调Handler
	 * @param networkType	手机联网类型
	 * @return
	 */
	public static boolean request(final int searchType, final HashMap<String, String> filterParams, final Handler handler, final String networkType) {
		if (IsBusy || filterParams == null)
			return false;
		IsBusy = true;
		
		new Thread() {
			public void run() {
				int count = retry;
				while (count > 0){
					try {

						//根据过滤选项拼接请求URL
						String requestURL = "";
						if(searchType == -1){
							//沿用上次搜索保存的search type
							if(currSearchType == SEARCH_TYPE_NEARBY){
								requestURL = SEARCH_URI_NEARBY;
							}else if(currSearchType == SEARCH_TYPE_LOCAL){
								requestURL = SEARCH_URI_LOCAL;
							}
						}else{
							if(searchType == SEARCH_TYPE_NEARBY){
								requestURL = SEARCH_URI_NEARBY;
							}else if(searchType == SEARCH_TYPE_LOCAL){
								requestURL = SEARCH_URI_LOCAL;
							}
							currSearchType = searchType;
						}
						requestURL = requestURL   + "&"
										+ "ak=" + ak
										+ "&geotable_id=" + geotable_id;
						//http://api.map.baidu.com/geosearch/v2/local?&ak=rjOY9xDVgrxTG5UtDlkfHgOEYhwBiv9x&geotable_id=179931&address=�����
						String filter = null;
						Iterator iter = filterParams.entrySet().iterator();
						while (iter.hasNext()) {
							Map.Entry entry = (Map.Entry) iter.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							
							if(key.equals("filter")){
								filter = value;
							}else{
								if(key.equals("region") && currSearchType == SEARCH_TYPE_NEARBY){
									continue;
								}
								requestURL = requestURL + "&" + key + "=" + value;
							}
						}
						
						if(filter != null && !filter.equals("")){
							//substring(3) 为了去掉"|" 的encode  "%7C"
							requestURL = requestURL + "&filter=" + filter.substring(3);
						}
						
						Log.d("DuanZuLog", "request url:" + requestURL);
						
						/*HttpGet httpRequest = new HttpGet(requestURL);
						HttpClient httpclient = new DefaultHttpClient();
						httpclient.getParams().setParameter(
								CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);
						httpclient.getParams().setParameter(
								CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);
						if(networkType.equals("cmwap")){
							HttpHost proxy = new HttpHost("10.0.0.172", 80, "http");
							httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
									proxy);
						}else if(networkType.equals("ctwap")){
							HttpHost proxy = new HttpHost("10.0.0.200", 80, "http");
							httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
									proxy);
						}*/
						URL url=new URL(requestURL);
						HttpURLConnection urlConnection=null;
						if(networkType.equals("cmwap")){
							Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.172", 80));
							urlConnection= (HttpURLConnection) url.openConnection(proxy);
							//HttpHost proxy = new HttpHost("10.0.0.172", 80, "http");
							/*httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
									proxy);*/
						}else if(networkType.equals("ctwap")){
							Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.200", 80));
							urlConnection= (HttpURLConnection) url.openConnection(proxy);

/*
							HttpHost proxy = new HttpHost("10.0.0.200", 80, "http");
							httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
									proxy);*/
						}
						urlConnection= (HttpURLConnection) url.openConnection();
						urlConnection.setConnectTimeout(TIME_OUT);
						//HttpResponse httpResponse = httpclient.execute(httpRequest);
						//int status = httpResponse.getStatusLine().getStatusCode();
						if (urlConnection.getResponseCode()==200) {
							InputStream is =urlConnection.getInputStream();
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] buffer = new byte[1024];
							int len = 0;
							while(-1 != (len = is.read(buffer))){
								baos.write(buffer,0,len);
								baos.flush();
							}
							String result= baos.toString("utf-8");
							/*String result = EntityUtils.toString(httpResponse
									.getEntity(), "utf-8");
							Header a = httpResponse.getEntity().getContentType();*/
							Message msgTmp = handler.obtainMessage(LBSActivity.MSG_NET_SUCC);
							msgTmp.obj =result;
							msgTmp.sendToTarget();
							

							break;
						} else {

							Message msgTmp = handler.obtainMessage(LBSActivity.MSG_NET_STATUS_ERROR);
							msgTmp.obj = "HttpStatus error";
							msgTmp.sendToTarget();
						}
					} catch (Exception e) {
						Log.e("DuanZuLog", "网络异常，请检查网络后重试！");
						e.printStackTrace();
					}
					count--;
				}
				
				if ( count <= 0 && handler != null){
					Message msgTmp =  handler.obtainMessage(LBSActivity.MSG_NET_TIMEOUT);
					msgTmp.sendToTarget();
				}
				
				IsBusy = false;
				
			}
		}.start();

		return true;
	}
	
}
