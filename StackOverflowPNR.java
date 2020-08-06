package in.javadomain;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StackOverflowPNR {
    public static void main(String args[]) {
        try {
        	String captcha = "24357";
        	String pnr1 = "4738866122";
        	String reqStr = "lccp_pnrno1=" + pnr1 + "&lccp_cap_value=" + captcha + "&lccp_capinp_value=" + captcha + "&submit=Please+Wait...";
        	StackOverflowPNR check = new StackOverflowPNR();
        	StringBuffer data = check.getPNRResponse(reqStr, "http://www.indianrail.gov.in/cgi_bin/inet_pnstat_cgi_1738.cgi");
        	
        	// giving the html output response string to jsoup
        	Document doc =  Jsoup.parse(data.toString());
        	String pnrNumber = null;
        	
        	/* Retrieving the PNR Number starts */
        	pnrNumber = doc.select("td.Enq_heading").text();
        	String subStrTillNumber = pnrNumber.substring(("You Queried For : PNR Number : ".length()), pnrNumber.length());
        	String onlyPNRNumber = subStrTillNumber.substring(0, subStrTillNumber.indexOf("(E - TICKET)"));
        	String finalPNR = onlyPNRNumber.replaceAll("-", "");
        	//System.out.println(finalPNR);
        	/* Retrieving the PNR Number ends */
        	
        	Map<String,String> valsOrderMap = new LinkedHashMap<String,String>();
        	List<String> trainDtls = new LinkedList<String>();
        	trainDtls.add("TRAIN_NUMBER");
        	trainDtls.add("TRAIN_NAME");
        	trainDtls.add("BOARDING_DATE");
        	trainDtls.add("FROM");
        	trainDtls.add("TO");
        	trainDtls.add("RESERVED_UPTO");
        	trainDtls.add("BOARDING_POINT");
        	trainDtls.add("CLASS");
        	
        	Elements allRequiredDtls = doc.select("table.table_border").select("tbody").select("TD.table_border_both");
        	for(int i=0;i<8;i++)
        	{
        		valsOrderMap.put(trainDtls.get(i), allRequiredDtls.get(i).text());
        	}
        	
        	for(Map.Entry<String, String> vals:valsOrderMap.entrySet()){
        		System.out.println(vals.getKey()+"  "+vals.getValue());
        	}
        	

        	int totalPass = doc.select("td.text_back_color").select("table#center_table").select("tbody").select("tr").size();
        	List<PassengerDetails> allPassLst = new LinkedList<PassengerDetails>();
        	Elements passAllDtls = doc.select("td.text_back_color").select("table#center_table").select("tbody");
        	PassengerDetails pdtls = new PassengerDetails();
        	for(int i=1;i<totalPass-3;i++){
        		Elements passAllItr = passAllDtls.select("tr").get(i).select("td.table_border_both");
        		pdtls = new PassengerDetails();
        		for(int j=0;j<passAllItr.size();j++){
        			if(j==0){
        			pdtls.setName(passAllItr.get(j).text());
        			}else if(j==1){
        			pdtls.setBookingStatus(passAllItr.get(j).text());
        			}else if(j==2){
        			pdtls.setCurrentStatus(passAllItr.get(j).text());
        			}
        		}
        		allPassLst.add(pdtls);
        	}
        	
        	for (PassengerDetails eachPass : allPassLst) {
				System.out.println("Pass Name:::"+eachPass.getName());
				System.out.println("Booking status:::"+eachPass.getBookingStatus());
				System.out.println("Current Status:::"+eachPass.getCurrentStatus());
			}
        	
        	
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public StringBuffer getPNRResponse(String reqStr, String urlAddr) throws Exception {
        String urlHost = null;
        int port;
        String method = null;
        try {
            URL url = new URL(urlAddr);
            urlHost = url.getHost();
            port = url.getPort();
            method = url.getFile();

            // validate port
            if(port == -1) {
                port = url.getDefaultPort();
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
        HttpProtocolParams.setUseExpectContinue(params, true);

        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        // Required protocol interceptors
        httpproc.addInterceptor(new RequestContent());
        httpproc.addInterceptor(new RequestTargetHost());
        // Recommended protocol interceptors
        httpproc.addInterceptor(new RequestConnControl());
        httpproc.addInterceptor(new RequestUserAgent());
        httpproc.addInterceptor(new RequestExpectContinue());

        HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
        HttpContext context = new BasicHttpContext(null);
        HttpHost host = new HttpHost(urlHost, port);
        DefaultHttpClientConnection conn = new DefaultHttpClientConnection();

        context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);
        @SuppressWarnings("unused")
        String resData = null;
        @SuppressWarnings("unused")
        String statusStr = null;
        StringBuffer buff = new StringBuffer();
        try {
            String REQ_METHOD = method;
            String[] targets = { REQ_METHOD };

            for (int i = 0; i < targets.length; i++) {
                if (!conn.isOpen()) {
                    Socket socket = new Socket(host.getHostName(), host.getPort());
                    conn.bind(socket, params);
                }
                BasicHttpEntityEnclosingRequest req = new BasicHttpEntityEnclosingRequest("POST", targets[i]);
                req.setEntity(new InputStreamEntity(new ByteArrayInputStream(reqStr.toString().getBytes()), reqStr.length()));
                req.setHeader("Content-Type", "application/x-www-form-urlencoded");
                req.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.75 Safari/535.7");
                req.setHeader("Cache-Control", "max-age=0");
                req.setHeader("Connection", "keep-alive");
                req.setHeader("Origin", "http://www.indianrail.gov.in");
                req.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                req.setHeader("Referer", "http://www.indianrail.gov.in/pnr_Enq.html");
                //req.setHeader("Accept-Encoding", "gzip,deflate,sdch");
                req.setHeader("Accept-Language", "en-US,en;q=0.8");
                req.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");


                httpexecutor.preProcess(req, httpproc, context);

                HttpResponse response = httpexecutor.execute(req, conn, context);
                response.setParams(params);
                httpexecutor.postProcess(response, httpproc, context);

                Header[] headers = response.getAllHeaders();
                for(int j=0; j<headers.length; j++) {
                    if(headers[j].getName().equalsIgnoreCase("ERROR_MSG")) {
                        resData = EntityUtils.toString(response.getEntity());
                    } 
                }
                statusStr = response.getStatusLine().toString();
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = null;
                if(in != null) {
                    reader = new BufferedReader(new InputStreamReader(in));
                }

                String line = null;
                while((line = reader.readLine()) != null) {
                    buff.append(line + "\n");
                }
                try {
                    in.close();
                } catch (Exception e) {}
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buff;
    }

}