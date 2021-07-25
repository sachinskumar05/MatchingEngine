package com.baml.matching.exchange.executions;

import com.baml.matching.types.ExecType;
import com.baml.matching.types.OrderType;
import lombok.Data;

public class EQExecutionReport implements ExectionReport {
    private final String execId ;
    private final String orderId ;
    private final ExecType execType;
    private final OrderType orderType;

    private final double execQty;
    private final double execPrice;

    @Data
    public static class ERBuilder {
        private  String execId ;
        private  String orderId ;
        private  ExecType execType;
        private  OrderType orderType;

        private  double execQty;
        private  double execPrice;

    }

    public EQExecutionReport(String execId, String orderId, ExecType execType, OrderType ordTyp, double execQty, double execPrice){

          this.execId = execId;
          this.orderId = orderId;
          this.execType = execType;
          this.orderType = ordTyp;

          this.execQty = execQty;
          this.execPrice = execPrice;

    }

    public String getExecId() {
        return execId;
    }

    public String getOrderId() {
        return orderId;
    }

    public ExecType getExecType() {
        return execType;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public double getExecQty() {
        return execQty;
    }

    public double getExecPrice() {
        return execPrice;
    }
}
