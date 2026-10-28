package LAB2;

import LABB1.Account;
import LABB1.Transaction;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface TransactionDAO {


        boolean CreateTable() throws SQLException;

        Transaction findByAccount(String id) throws SQLException;

        List<Transaction> findAll() throws SQLException;

        List<Transaction> findAllByAccount(String id) throws SQLException; // added: full history


        List<Transaction> findByAccountAndDate(String accountId, LocalDate from, LocalDate to) throws SQLException;

        List<String[]> dailySummary(LocalDate day) throws SQLException;   // each row: type, count, total


}