package LAB2;

import LABB1.Customer;

import java.sql.SQLException;
import java.util.List;

public interface CustomerDAO {

    boolean CreateTable()throws SQLException;
    Customer registerCustomer(Customer customer) throws SQLException;
    Customer findById(String id) throws SQLException;
    List<Customer> findAll() throws SQLException;
    boolean updateCustomer(Customer customer) throws SQLException;
    boolean deleteCustomer(String id) throws SQLException;

    // update ONE column only
    boolean updateName(String id, String name) throws SQLException;
    boolean updateEmail(String id, String email) throws SQLException;
    boolean updatePhone(String id, String phone) throws SQLException;
}