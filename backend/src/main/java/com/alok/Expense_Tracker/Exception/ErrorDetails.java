package com.alok.Expense_Tracker.Exception;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
@Data
public class ErrorDetails {
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime timestamp;
    public HttpStatus status;
    public String error;

    public ErrorDetails(){
        this.timestamp=LocalDateTime.now();
    }

    public ErrorDetails(String error,HttpStatus status){
        this();
        this.error=error;
        this.status=status;
    }


}
