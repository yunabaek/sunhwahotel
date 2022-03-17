package co.kr.hotel.dao;

import java.util.ArrayList;
import java.util.HashMap;

import co.kr.hotel.dto.RoomDTO;

public interface ReserveDAO {

	ArrayList<RoomDTO> toReservelist(String checkin_date, String checkout_date, int cnt);

	ArrayList<HashMap<String, String>> reservation_option();

	ArrayList<HashMap<String, String>> reservation_product();

	int useable(String loginId);

}
