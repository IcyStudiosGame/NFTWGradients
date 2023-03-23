package com.nftworlds.gradients.sql.func;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementPreparer {

    void prepare(PreparedStatement statement) throws SQLException;

}
