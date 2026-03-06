package com.commlink.citl_nid_sdk.db;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.commlink.citl_nid_sdk.model.NidInfoEntity;

import java.util.List;

@Dao
public interface NidInfoDao {

    @Insert
    long insert(NidInfoEntity nidInfo);

    @Query("SELECT * FROM nid_info ORDER BY createdAt DESC")
    List<NidInfoEntity> getAll();

    @Query("SELECT * FROM nid_info WHERE id = :id")
    NidInfoEntity getById(int id);

    @Query("DELETE FROM nid_info")
    void deleteAll();
}
