package com.scnu.common.to.mq;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockLockedTo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; //库存工作单的id
    private StockDetailTo detailTo; //工作单详情

}
