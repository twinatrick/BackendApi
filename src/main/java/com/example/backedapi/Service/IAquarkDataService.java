package com.example.backedapi.Service;

import com.example.backedapi.model.Vo.aquarkUse.AquarkDataRaw;
import com.example.backedapi.model.Vo.aquarkUse.AverageAquark;
import com.example.backedapi.model.Vo.aquarkUse.CriteriaAPIFilter;

import java.util.Date;
import java.util.List;

public interface IAquarkDataService {
    List<AquarkDataRaw> getAquarkData();

    List<String> getColumnNameList();

    List<AverageAquark> getAverageAquark(Date start, Date end);

    List<AquarkDataRaw> getAquarkDataWithFilter(List<CriteriaAPIFilter> fillterList);

    boolean insertAquarkData(List<AquarkDataRaw> aquarkDataList);

    AquarkDataRaw insertAquarkData(AquarkDataRaw aquarkData);

    AquarkDataRaw getAquarkData(AquarkDataRaw aquarkData);

    AquarkDataRaw updateAquarkData(AquarkDataRaw aquarkData);
}
