package com.example.backedapi.model.db;

import com.example.backedapi.model.Vo.aquarkUse.AquarkDataRaw;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
public class AquarkData  implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "`key`")
    private UUID key;

    private String station_id;//站點
    private String CSQ;
    private Date trans_time;
    private float rain_d;//日累積雨量

    private float moisture;//濕度
    private float temperature;//溫度

    private float echo; //水位空高

    private float waterSpeedAquark;//水流速

    private boolean isPeak;//是否為尖峰

    private float v1;//鋰電池電壓
    private float v2;
    private float v3;
    private float v4;
    private float v5;//太陽能板 1
    private float v6;//太陽能板 1
    private float v7;

    public AquarkDataRaw toVo() {
        AquarkDataRaw aquarkDataRaw = new AquarkDataRaw();
        aquarkDataRaw.setKey(this.key.toString());
        aquarkDataRaw.setStation_id(this.station_id);
        aquarkDataRaw.setCSQ(this.CSQ);
        aquarkDataRaw.setTrans_time(this.trans_time);
        aquarkDataRaw.setRain_d(this.rain_d);
        aquarkDataRaw.setMoisture(this.moisture);
        aquarkDataRaw.setTemperature(this.temperature);
        aquarkDataRaw.setEcho(this.echo);
        aquarkDataRaw.setWaterSpeedAquark(this.waterSpeedAquark);
        aquarkDataRaw.setV1(this.v1);
        aquarkDataRaw.setV2(this.v2);
        aquarkDataRaw.setV3(this.v3);
        aquarkDataRaw.setV4(this.v4);
        aquarkDataRaw.setV5(this.v5);
        aquarkDataRaw.setV6(this.v6);
        aquarkDataRaw.setV7(this.v7);
        aquarkDataRaw.setPeak(this.isPeak);

        return aquarkDataRaw;
    }
}
