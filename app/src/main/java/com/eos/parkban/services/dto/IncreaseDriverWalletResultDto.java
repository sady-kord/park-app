package com.eos.parkban.services.dto;

import com.google.gson.annotations.SerializedName;

public class IncreaseDriverWalletResultDto extends ResultDto {

    @SerializedName("Values")
    private IncreaseDriverWalletDto value;

    public IncreaseDriverWalletDto getValue() {
        return value;
    }

    public void setValue(IncreaseDriverWalletDto value) {
        this.value = value;
    }

    public static class IncreaseDriverWalletDto {

        @SerializedName("ErrorMessage")
        private String errorMessage;

        @SerializedName("PhoneNumber")
        private long phoneNumber;

        @SerializedName("RealPhoneNumber")
        private long realPhoneNumber;

        @SerializedName("Amount")
        private long amount;

        @SerializedName("Plate")
        private String plate;

        @SerializedName("ReceiptCode")
        private String receiptCode;

        @SerializedName("DriverWalletCashAmount")
        private long driverWalletCashAmount;

        @SerializedName("QRCodeBase64")
        private String QRCodeBase64;

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(long phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public long getRealPhoneNumber() {
            return realPhoneNumber;
        }

        public void setRealPhoneNumber(long realPhoneNumber) {
            this.realPhoneNumber = realPhoneNumber;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public String getPlate() {
            return plate;
        }

        public void setPlate(String plate) {
            this.plate = plate;
        }

        public String getReceiptCode() {
            return receiptCode;
        }

        public void setReceiptCode(String receiptCode) {
            this.receiptCode = receiptCode;
        }

        public long getDriverWalletCashAmount() {
            return driverWalletCashAmount;
        }

        public void setDriverWalletCashAmount(long driverWalletCashAmount) {
            this.driverWalletCashAmount = driverWalletCashAmount;
        }

        public String getQRCodeBase64() {
            return QRCodeBase64;
        }

        public void setQRCodeBase64(String QRCodeBase64) {
            this.QRCodeBase64 = QRCodeBase64;
        }
    }
}
