package com.yubzhou.pojo.fieldmatch;


import lombok.Data;

@Data
public class User {

	private Profile profile;

	private Profile confirmProfile;


	@Data
	public static class Profile {
		private String email;
	}
}
