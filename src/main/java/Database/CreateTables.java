package Database;

import java.sql.Connection;

public class CreateTables {

    public static boolean createTableCouriers(Connection conn) {
        /*
        create table couriers
            (
	            id bigserial,
	            name varchar not null,
	            username varchar,
	            privilege varchar
            );

        create unique index couriers_id_uindex
	        on couriers (id);

        alter table couriers
	        add constraint couriers_pk
		        primary key (id);

         */
        return false;
    }

    public static boolean createTableOrders(Connection conn) {
        /*
        create table orders
            (
	            order_id bigserial,
	            order_text text not null,
	            price int default 0,
	            courier_id bigint not null
		            constraint orders_couriers_id_fk
			            references couriers
            );

        create unique index orders_order_id_uindex
	        on orders (order_id);

        alter table orders
	        add constraint orders_pk
		        primary key (order_id);
         */
        return false;
    }
}
