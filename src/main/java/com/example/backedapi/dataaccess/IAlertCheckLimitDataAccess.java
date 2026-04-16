package com.example.backedapi.dataaccess;

import com.example.backedapi.Enity.AlertCheckLimit;

import java.util.List;

/**
 * Data access interface for AlertCheckLimit entity operations.
 * Abstracts AlertCheckLimitRepository operations for service layer.
 */
public interface IAlertCheckLimitDataAccess {

    /**
     * Find all alert check limits.
     *
     * @return list of all alert check limits
     */
    List<AlertCheckLimit> findAll();

    /**
     * Find alert check limits by table and column.
     *
     * @param tableName table name
     * @param columnName column name
     * @return list of matching limits
     */
    List<AlertCheckLimit> findByTableNameAndColumnName(String tableName, String columnName);

    /**
     * Save an alert check limit.
     *
     * @param alertCheckLimit limit to save
     * @return saved entity
     */
    AlertCheckLimit save(AlertCheckLimit alertCheckLimit);

    /**
     * Delete an alert check limit.
     *
     * @param alertCheckLimit limit to delete
     */
    void delete(AlertCheckLimit alertCheckLimit);
}
