package co.kr.hotel.controller;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.kr.hotel.dto.MemberDTO;
import co.kr.hotel.dto.ReserveDTO;
import co.kr.hotel.dto.RoomDTO;
import co.kr.hotel.service.ReserveService;

@Controller
public class ReserveController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired ReserveService service;

	
	@RequestMapping(value = "/reservation", method = RequestMethod.GET)
	public String reservation(Model model,HttpSession session) {
		
		
		
		logger.info("reservation 페이지 요청이 들어옴 ");
		
		return "reservation";
	}
	
	
	//메인페이지 -> 객실 예약하기 페이지 요청 유선화 START 20220311
	
	@RequestMapping(value = "/reserveRoomslist", method = RequestMethod.GET)
	public String rooms( Model model) {
		
		
		
		
		logger.info("rooms로 요청이 들어옴 ");
		
		return "reserveRoomslist";
	}
	
	//메인페이지 -> 객실 예약하기 페이지 요청 유선화 END 20220311
	
	
	//객실 옵션 페이지 이지선 START 220315
	
	 //객실 옵션 페이지 접속 리스트 불러오기	
	 @RequestMapping(value = "/reservation_option", method = RequestMethod.POST)
		public String reservation_option(Model model,HttpSession session,String params) { 
			logger.info("reservation_option 요청");
			
			
			
			logger.info("받아온 값 확인 {}",params);
			
			//형민- 객실정보 03.21 합침
			List<Map<String,Object>> dataList = new ArrayList<> ();
			JsonArray ja = new JsonArray();
			JsonParser jsonParser = new JsonParser(); //List를 String으로 변환
			ja = (JsonArray) jsonParser.parse(params);
			int len = ja.size();
			   for (int i=0;i<len;i++){
				 
				 Map<String,Object> map = new HashMap<String,Object>();
				 JsonObject jsonObj = (JsonObject) ja.get(i);
				 
				 Gson gson = new Gson();
				 map = (Map<String,Object>) gson.fromJson(jsonObj.toString(), map.getClass());
				 dataList.add(map);
			   } 								
			  model.addAttribute("reserveData", dataList); //객실타입/객실 침대타입/인원 수/가격
			//객실정보 끝
			  
			  
			  
			//아이디
			String loginId = "testid";
			model.addAttribute("loginId",loginId);

			//체크인날짜,체크아웃날짜
			model.addAttribute("checkindate",dataList.get(0).get("checkin").toString()); 
			model.addAttribute("checkoutdate",dataList.get(0).get("checkout").toString()); 
			
			//옵션 값 불러오기
			ArrayList<HashMap<String, String>> option = service.reservation_option();
			logger.info("받아온 값 확인 {}",option);
			model.addAttribute("option",option);
			
			//마일리지 상품 4가지 불러오기
			ArrayList<HashMap<String, String>> product = service.reservation_product();
			logger.info("받아온 값 확인 {}",product);
			model.addAttribute("product",product);
			
			
			//회원님의 마일리지 불러오기
			//테스트하고 로그인 세션체크로 바꾸기 ★
			//String loginId = (String)session.getAttribute();
			int useable = service.useable(loginId);
			model.addAttribute("useable", useable);
			
			
			//회원정보,카드정보 가져오기
			MemberDTO memberDto = new MemberDTO();
			memberDto = service.reservation_memInfo(loginId);
			
			
			String mem_card = memberDto.getCredit_type();
			model.addAttribute("mem_card",mem_card);
			if(mem_card != null) {
			String cardBank = service.mem_card(mem_card);
			logger.info("cardBank: "+cardBank);
			memberDto.setCredit_type(cardBank);
			}
			model.addAttribute("memInfo", memberDto);
			return "reservation_option"; 
	 }
	
	 
	 
	 
	 
	 
	//예약 옵션/결제 선택 후 예약 요청  START
	@RequestMapping(value = "/toReserveOrder", method = RequestMethod.POST)
		public String toReserveOrder(Model model, HttpSession session,
				@RequestParam Map<String, String> params) {
		logger.info("toReserveOrder 요청 : {}",params);
		
		//String userId = "seon119";
		//params.replace("userId", userId);
		

		String loginId = params.get("loginId"); //세션으로 변경★
		int extrabed_price = service.extrabed_price();//엑스트라베드 가격
		int breakfast_price = service.breakfast_price();//조식 가격
		
		logger.info("가격1: "+extrabed_price);
		logger.info("가격2: "+breakfast_price);
		
		
		//객실 1 START 
		ReserveDTO dto = new ReserveDTO();
		dto.setMem_id(params.get("loginId"));
		
		String reserveNum = "S"+System.currentTimeMillis();
		dto.setReserve_num(reserveNum);
		
		dto.setReserve_state("1");
		dto.setCheckindate(params.get("checkindate"));
		dto.setCheckoutdate(params.get("checkoutdate"));
		dto.setAdult_cnt(Integer.parseInt(params.get("people_1")));//인원수
		
		int extrabed_cnt = Integer.parseInt(params.get("option1_cnt_1"));//엑스트라베드 수량
		int breakfast_cnt = Integer.parseInt(params.get("option2_cnt_1"));//조식 수량
		dto.setExtrabed_cnt(extrabed_cnt);
		dto.setBreakfast_cnt(breakfast_cnt);
		
		String add = params.get("ADD_1");
		if(add == null) {
			add = "없음";
		}
		dto.setAdd_requests(add);//추가요청사항
		

		
		
		
		// 빈 객실 가져오기 유선화 START 22.03.21
		RoomDTO roomDto = new RoomDTO();
		// 파라미터를 가져온다.
		
		int room_type = Integer.parseInt(params.get("room_type_1"));
		int bed_type = Integer.parseInt(params.get("room_bed_1"));
		int roomCnt = Integer.parseInt(params.get("people_1"));
		
		
		roomDto.setRoom_type(room_type);
		roomDto.setBed_type(bed_type);
		roomDto.setRoomCnt(roomCnt);
		roomDto.setCheckindate(params.get("checkindate"));
		roomDto.setCheckoutdate(params.get("checkoutdate"));
		
		ArrayList<RoomDTO> roomIdx = service.roomIdx(roomDto);
		Iterator<RoomDTO> iterroomIdx = roomIdx.iterator();
		RoomDTO roomDtoTwo = null;
		while(iterroomIdx.hasNext()) {
			
			roomDtoTwo = iterroomIdx.next();
			String roomIdx2 = roomDtoTwo.getRoom_num();
			logger.info("roomDtoTwo : "+roomDtoTwo.getRoom_num());
			dto.setRoom_num(roomIdx2); //객실번호  DTO 담기
		}
			
		logger.info("roomIdx"+roomIdx);
		
		// 빈 객실 가져오기 유선화 END 22.03.21
		
		
		
		service.roomOne(dto); //예약insert
		
		
		
		
		
		
		
		int reserve_idx = dto.getReserve_idx();
		logger.info("reserve_idx : "+reserve_idx);

		if(reserve_idx > 0 ) { //성공 예약번호 
			logger.info("마일리지상품 히스토리,회원 마일리지 내역 저장 시작");

			int productTotal = 0;
			
			for (int i = 1; i < 5; i++) {
				if(Integer.parseInt(params.get("p"+i+"_cnt_1")) != 0) {//선택한 마일리지 상품 수량이 0이 아니라면...
					logger.info("선택한 상품 있음");
					String product_num = params.get("product"+i+"_1");
					int product_price = service.product_price(product_num);
					//* 가격은 DB에서 가져온다.
					
					int product_cnt = Integer.parseInt(params.get("p"+i+"_cnt_1"));
					productTotal -= product_price*product_cnt;
					
					service.roomOneCart(reserve_idx,product_cnt,product_num); //마일리지 상품히스토리(cart) Insert

					
				}
				
			}
			
			logger.info("productTotal : "+productTotal);
			if(productTotal != 0) {
				logger.info("객실 1 마일리지 총 구입금액 : "+productTotal);//mileage_price (-값)
				int useable = service.useable(loginId);//DB 회원의 마일리지 잔액		
				int mileage_useable = useable+productTotal;//회원 마일리지 잔액 - 객실별 상품 총 구입액 => mileage_useable
				logger.info("남은 사용가능액 계산값 : "+mileage_useable);
				service.buyMileageminus(loginId,productTotal,mileage_useable); //마일리지 내역 Insert : 상품 구입액만큼 차감
			}
			
			
			 //객실별 결제 insert 시작	
		     String Pay_num = "P"+System.currentTimeMillis();//결제번호
		     String credit_num = params.get("credit_num");//카드번호
		     int credit_validity =  Integer.parseInt(params.get("credit_valid"));
		     String credit_type = params.get("credit_type");
		     //룸가격 + 엑베가격*수량 + 조식가격*수량
		     int room_price = service.room_price(reserve_idx);//룸가격
		     int pay_price = room_price +(extrabed_price * extrabed_cnt) + (breakfast_price * breakfast_cnt);
		     int pay_mile = Math.abs(productTotal);//(+) 마일리지 사용금액
		     int amount = pay_price + pay_mile;
		     
		     service.roomPay(reserve_idx,Pay_num,credit_num,credit_validity,credit_type,pay_price,pay_mile,amount);
		     //객실 결제 끝
		     
			 //회원 결제 마일리지 적립(최종가*회원등급, 객실별아님)
				int mem_gradeRate = service.rate(loginId);//회원등급 불러오기
				int cardTotal = Integer.parseInt(params.get("cardTotal")); //카드사용금액
				int mileageSave = cardTotal*mem_gradeRate/100; //적립 마일리지 금액 계산값
				int useable = service.useable(loginId); //회원 사용가능금액
				int useableSave = useable+mileageSave; //사용가능금액 계산값
				service.mileageSave(loginId,mileageSave,useableSave);
			 
				
				
		}
	
		
		
		//객실 1 END
		
		
		 
		//room_num_2 가 있으면 객실2 INSERT (반복작업) _2로 바꿔준다.
//		if(params.get("room_type_2") != null) {
//			
//			logger.info("객실2 insert 시작");
//			
//			
//			//객실 2 START 
//			ReserveDTO dto2 = new ReserveDTO();
//			dto2.setMem_id(params.get("loginId"));
//			dto2.setReserve_num(reserveNum);
//			
//			dto2.setReserve_state("1");
//			dto2.setCheckindate(params.get("checkindate"));
//			dto2.setCheckoutdate(params.get("checkoutdate"));
//			dto2.setAdult_cnt(Integer.parseInt(params.get("people_2")));//인원수
//			
//			int extrabed_cnt2 = Integer.parseInt(params.get("option1_cnt_2"));//엑스트라베드 수량
//			int breakfast_cnt2 = Integer.parseInt(params.get("option2_cnt_2"));//조식 수량
//			dto2.setExtrabed_cnt(extrabed_cnt2);
//			dto2.setBreakfast_cnt(breakfast_cnt2);
//			
//			String add2 = params.get("ADD_2");
//			if(add2 == null) {
//				add2 = "없음";
//			}
//			dto2.setAdd_requests(add2);//추가요청사항
//			
//			
			

			
		
		
		
		
		return "toReserveOrder";
	}


	//객실 옵션 페이지 이지선 END 220315
	
	
	@RequestMapping(value = "/doReservation", method = RequestMethod.GET)
	public String doReservation(Model model,HttpSession session) {
		
		return "doReservation";
	}
	


	
	
	
	
	
	
	
	
	
	
		
}
