package com.eos.parkban.services.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginResultDto extends ResultDto {

    @SerializedName("Values")
    private Parkban value;

    public Parkban getValue() {
        return value;
    }

    public void setValue(Parkban value) {
        this.value = value;
    }

    public static class Parkban{

        @SerializedName("UserId")
        private long parkbanId;

        @SerializedName("Token")
        private String token;

        @SerializedName("OtherValues")
        private List<ParkingSpaceDto> parkingSpaces;

        private String userName;

        private String password;

        public long getParkbanId() {
            return parkbanId;
        }

        public void setParkbanId(long parkbanId) {
            this.parkbanId = parkbanId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<ParkingSpaceDto> getParkingSpaces() {
            return parkingSpaces;
        }

        public void setParkingSpaces(List<ParkingSpaceDto> parkingSpaces) {
            this.parkingSpaces = parkingSpaces;
        }
    }
}
