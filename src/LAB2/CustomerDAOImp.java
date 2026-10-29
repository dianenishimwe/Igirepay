package LAB2;

import LABB1.Customer;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImp implements CustomerDAO {

    @Override
    public boolean CreateTable() throws SQLException{
        //try {
        Connection connection = DBConnection.getConnection();
        Statement statement= connection.createStatement();

        String CreateSql = """ 
                    create table customer(customer_Id varchar(50) primary key,
                    customer_Name varchar(255) not null,
                    email varchar(255) not null,
                    phone_number varchar(50) not null)
                    
                    """;

        statement.execute(CreateSql);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        return true;
    }



    @Override
    public Customer registerCustomer(Customer customer) throws SQLException {
        Connection connection = DBConnection.getConnection();


        String insertSql= """
                insert into customer(customer_Id,customer_Name,email,phone_number) 
                values(?,?,?,?)
               """;
        PreparedStatement st= connection.prepareStatement(insertSql);
        st.setString(1,customer.getCustomerId());
        st.setString(2,customer.getCustomerName());
        st.setString(3,customer.getEmail());
        st.setString(4,customer.getPhoneNumber());

        st.executeUpdate();


        return customer;

    }

    @Override

    public Customer findById(String id) throws SQLException {
        String findCustomerSql = "select * from customer where customer_Id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(findCustomerSql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(resultSet.getString("customer_Id"));
                    customer.setCustomerName(resultSet.getString("customer_Name"));
                    customer.setEmail(resultSet.getString("email"));
                    customer.setPhoneNumber(resultSet.getString("phone_number"));
                    return customer;
                }
            }
        }
        return null;
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        String display="select * from customer";
        Connection connection= DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(display);

        ResultSet resultSet = statement.executeQuery();

        List<Customer> allCustomer=new ArrayList<>();
        if (resultSet.next()) {
            Customer customer = new Customer();
            customer.setCustomerId(resultSet.getString("customer_Id"));
            customer.setCustomerName(resultSet.getString("customer_Name"));
            customer.setEmail(resultSet.getString("email"));
            customer.setPhoneNumber(resultSet.getString("phone_number"));

            return List.of(customer);


        }
        return null;
    }


    @Override
    public boolean updateCustomer(Customer customer) throws SQLException {
        String updateSql = """
            update customer
            set customer_Name = ?, email = ?, phone_number = ?
            where customer_Id = ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setString(1, customer.getCustomerName());   // 1st ?
            statement.setString(2, customer.getEmail());          // 2nd ?
            statement.setString(3, customer.getPhoneNumber());    // 3rd ?
            statement.setString(4, customer.getCustomerId());     // 4th ? (WHERE)

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                System.out.println("Customer " + customer.getCustomerId() + " is not found");
                return false;
            }

            System.out.println("Customer " + customer.getCustomerId() + " updated");
            return true;
        }
    }

    @Override
    public boolean deleteCustomer(String id) throws SQLException {
        String deleteSql= "delete from customer where customer_id =?";

        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(deleteSql);

        statement.setString(1, id);
        statement.executeUpdate();

        return true;
    }

    @Override
    public boolean updateName(String id, String name) throws SQLException {
        String updateSql = "update customer set customer_Name = ? where customer_Id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setString(1, name);   // 1st ?
            statement.setString(2, id);     // 2nd ? (WHERE)

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateEmail(String id, String email) throws SQLException {
        String updateSql = "update customer set email = ? where customer_Id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setString(1, email);  // 1st ?
            statement.setString(2, id);     // 2nd ? (WHERE)

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePhone(String id, String phone) throws SQLException {
        String updateSql = "update customer set phone_number = ? where customer_Id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setString(1, phone);  // 1st ?
            statement.setString(2, id);     // 2nd ? (WHERE)

            return statement.executeUpdate() > 0;
        }
    }


}