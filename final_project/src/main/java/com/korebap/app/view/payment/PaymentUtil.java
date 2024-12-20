package com.korebap.app.view.payment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import org.apache.tomcat.util.json.JSONParser;
//import org.apache.tomcat.util.json.ParseException;

import com.korebap.app.biz.payment.PaymentInfo;

public class PaymentUtil {
	private final static String IMP_KEY = "IMP_KEY";
	private final static String IMP_SECRET = "IMP_SECRET";


	// 토큰 발급 요청
	public static PaymentInfo portOne_code() {
		PaymentInfo paymentInfo = new PaymentInfo();
		HttpRequest request = HttpRequest.newBuilder()
			    .uri(URI.create("https://api.iamport.kr/users/getToken"))
			    .header("Content-Type", "application/json")
			    .method("POST", HttpRequest.BodyPublishers.ofString("{\"imp_key\":\"" + IMP_KEY + "\",\"imp_secret\":\"" + IMP_SECRET + "\"}"))
			    .build();
			HttpResponse<String> response = null;
			try {
				response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			} catch (IOException | InterruptedException e) {
				System.out.println("토큰 요청 실패");
				e.printStackTrace();
			}
			System.out.println(response.body());
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = null;
			try {
				jsonObject = (JSONObject) parser.parse(response.body());
			} catch (ParseException e) {
				System.out.println("json 변환 실패");
				e.printStackTrace();
			}
			JSONObject responseObject = (JSONObject) jsonObject.get("response");
			String token = (String)responseObject.get("access_token");
			paymentInfo.setToken(token);
			return paymentInfo;
	}


	// 결제 단건 조회
	public static PaymentInfo paymentTest(PaymentInfo paymentInfo) {
		// HTTP 요청 생성
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.iamport.kr/payments/" + paymentInfo.getImp_uid()))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + paymentInfo.getToken())
				.GET() // GET 요청
				.build();

		HttpResponse<String> response = null;
		// HTTP 요청 보내기 및 응답 받기
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			paymentInfo.setResponse(response);
			if (response != null) {
				System.out.println("응답이 !null임");
			}
			else {
				System.out.println("응답이 null임");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return paymentInfo;
	}

	// 사전 검증 등록
	public static boolean prepare(PaymentInfo paymentInfo) {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.iamport.kr/payments/prepare"))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + paymentInfo.getToken())
				.method("POST", HttpRequest.BodyPublishers.ofString("{\"merchant_uid\":\"" + paymentInfo.getMerchant_uid() + "\",\"amount\":" + paymentInfo.getAmount() + "}"))
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("사전등록 : "+response.body());
		return true;
	}

	// 사전 검증 조회
	public static PaymentInfo prepareReult(PaymentInfo paymentInfo) {
		// HTTP 요청 생성
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.iamport.kr/payments/prepare/"+paymentInfo.getMerchant_uid()+"?_token="+paymentInfo.getToken()))
				.header("Content-Type", "application/json")
				//.header("Authorization", "Bearer "+paymentInfo.getToken())
				.method("GET", HttpRequest.BodyPublishers.ofString("{}"))
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		JSONParser parser = new JSONParser();
		int amount_res = -1;
		try {
			System.out.println(response.body());
			JSONObject jsonObject = (JSONObject) parser.parse(response.body());

			JSONObject responseObject = (JSONObject) jsonObject.get("response");

			if(responseObject != null) {
				Long amount = (Long) responseObject.get("amount");
				amount_res = amount.intValue();
				paymentInfo.setAmount(amount_res);
			}
			else {
				System.out.println("Response 객체가 null입니다.");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println("사전등록조회 : "+response.body());
		return paymentInfo;
	}

	// 결제 취소
	public static boolean cancelPayment(PaymentInfo paymentInfo) {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.iamport.kr/payments/cancel?_token="+paymentInfo.getToken()))
				.header("Content-Type", "application/json")
				//.header("Authorization", "Bearer "+paymentInfo.getToken())
				.method("POST", HttpRequest.BodyPublishers.ofString("{\"merchant_uid\":\"" + paymentInfo.getMerchant_uid() + "\",\"checksum\":" + paymentInfo.getAmount() + "}"))
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println(response.body());
		return true;
	}

	//	public static void paymentTest(String token, String imp_uid) {
	//		HttpRequest request = HttpRequest.newBuilder()
	//				.uri(URI.create("https://api.iamport.kr/payments/"+imp_uid))
	//				.header("Content-Type", "application/json")
	//				.header("Authorization", "Bearer "+token)
	//				.method("GET", HttpRequest.BodyPublishers.ofString("{}"))
	//				.build();
	//		HttpResponse<String> response = null;
	//		try {
	//			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	//			System.out.println(response.body());
	//		} catch (IOException | InterruptedException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		String jsonString = response.body(); // 응답 본문에서 JSON 문자열을 추출
	//
	//		// JSON 파서 및 파싱
	//		JSONParser parser = new JSONParser();
	//		JSONObject jsonObject = null;
	//		try {
	//			jsonObject = (JSONObject) parser.parse(jsonString);
	//		} catch (ParseException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//		// 'response' 객체 가져오기
	//		JSONObject responseObject = (JSONObject) jsonObject.get("response");
	//
	//		// 필요한 값 추출
	//		long amount = (long) responseObject.get("amount");
	//		String buyerName = (String) responseObject.get("buyer_name");
	//		String receiptUrl = (String) responseObject.get("receipt_url");
	//		String cardName = (String) responseObject.get("card_name");
	//
	//		// 출력
	//		System.out.println("Amount: " + amount);
	//		System.out.println("Buyer Name: " + buyerName);
	//		System.out.println("Receipt URL: " + receiptUrl);
	//		System.out.println("Card Name: " + cardName);
	//
	//}
	//
	//
	//	public static void main(String[] args) {
	//		String token = Test.portOne_code();
	//		System.out.println("portone token : "+token);
	//		//		Test.test(token);
	//
	//	}
}
