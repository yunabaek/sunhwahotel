package co.kr.hotel.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.kr.hotel.dao.ReserveDAO;
import co.kr.hotel.dto.RoomDTO;

@Service
public class ReserveService {

	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired ReserveDAO reserveDao;
	//객실 예약 리스트 START 유선화 20220311
	public ArrayList<RoomDTO> toReservelist(String checkin_date, String checkout_date, int cnt) {
		// TODO Auto
		
		return reserveDao.toReservelist(checkin_date,checkout_date,cnt);
	}
	//객실 예약 리스트 END 유선화 20220311
	
	//객실 옵션 페이지 이지선 START 220315
	
	public ArrayList<HashMap<String, String>> reservation_option() {
		ArrayList<HashMap<String, String>> option = reserveDao.reservation_option();
		return option;
	}

	public ArrayList<HashMap<String, String>> reservation_product() {
		ArrayList<HashMap<String, String>> product = reserveDao.reservation_product();
		return product;
	}

	public int useable(String loginId) {
		
		return reserveDao.useable(loginId);
	}
	
	
	
	//객실 옵션 페이지 이지선 END 220315
}
