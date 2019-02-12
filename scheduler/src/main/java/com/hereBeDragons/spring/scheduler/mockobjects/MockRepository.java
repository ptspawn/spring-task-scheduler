package com.hereBeDragons.spring.scheduler.mockobjects;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MockRepository {

	private static final int NUMBER_CAP = 10000;
	private static final int DB_SIZE = 2000;
	private static final List<Integer> database = new ArrayList<>();


	public MockRepository(){

		for (int i=0;i < DB_SIZE; i++)
			database.add((int) (Math.random()*NUMBER_CAP));
	}

	public List<Integer> findAllNumbersAbove(int limit){

		List<Integer> matchNumbers = new ArrayList<>();

		for(Integer integer: database)
			if (integer>limit)
				matchNumbers.add(integer);

		return matchNumbers;
	}

	public List<Integer> findAllEvenNumbers(){
		List<Integer> matchNumbers = new ArrayList<>();

		for(Integer integer: database)
			if (integer % 2 == 0)
				matchNumbers.add(integer);

		return matchNumbers;
	}


}
