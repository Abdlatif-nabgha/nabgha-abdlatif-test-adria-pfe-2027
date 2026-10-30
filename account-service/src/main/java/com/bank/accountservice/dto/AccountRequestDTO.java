package com.bank.accountservice.dto;


public record AccountRequestDTO(String customerReference, String currency, double balance) {

}
