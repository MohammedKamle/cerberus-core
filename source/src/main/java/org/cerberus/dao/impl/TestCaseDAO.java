/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCase;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Used to manage TestCase table
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/12/2012
 * @since 0.9.0
 */
@Repository
public class TestCaseDAO implements ITestCaseDAO {

    /**
     * Class used to manage connection.
     *
     * @see org.cerberus.database.DatabaseSpring
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTCase factoryTestCase;

    /**
     * Get summary information of all test cases of one group.
     * <p/>
     * Used to display list of test cases on drop-down list
     *
     * @param test Name of test group.
     * @return List with a list of 3 strings (name of test case, type of
     * application, description of test case).
     */
    @Override
    public List<TCase> findTestCaseByTest(String test) {
        List<TCase> list = null;
        final String query = "SELECT * FROM testcase WHERE test = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    /**
     * Get test case information.
     *
     * @param test Name of test group.
     * @param testCase Name of test case.
     * @return TestCase object or null.
     * @see org.cerberus.entity.TestCase
     */
    @Override
    public TCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        boolean throwExcep = false;
        TCase result = null;
        final String query = "SELECT * FROM testcase WHERE test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = this.loadTestCaseFromResultSet(resultSet);
                    } else {
                        throwExcep = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        boolean res = false;
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ?, tc.`function` = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, testCase.getApplication());
                preStat.setString(2, testCase.getProject());
                preStat.setString(3, testCase.getDescription());
                preStat.setString(4, testCase.isRunQA() ? "Y" : "N");
                preStat.setString(5, testCase.isRunUAT() ? "Y" : "N");
                preStat.setString(6, testCase.isRunPROD() ? "Y" : "N");
                preStat.setString(7, Integer.toString(testCase.getPriority()));
                preStat.setString(8, testCase.getStatus());
                preStat.setString(9, testCase.isActive() ? "Y" : "N");
                preStat.setString(10, testCase.getShortDescription());
                preStat.setString(11, testCase.getGroup());
                preStat.setString(12, testCase.getHowTo());
                preStat.setString(13, testCase.getComment());
                preStat.setString(14, testCase.getTicket());
                preStat.setString(15, testCase.getFromSprint());
                preStat.setString(16, testCase.getFromRevision());
                preStat.setString(17, testCase.getToSprint());
                preStat.setString(18, testCase.getToRevision());
                preStat.setString(19, testCase.getBugID());
                preStat.setString(20, testCase.getTargetSprint());
                preStat.setString(21, testCase.getImplementer());
                preStat.setString(22, testCase.getLastModifier());
                preStat.setString(23, testCase.getTargetRevision());
                preStat.setString(24, testCase.getFunction());
                preStat.setString(25, testCase.getTest());
                preStat.setString(26, testCase.getTestCase());

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean updateTestCaseInformationCountries(TestCase tc) {
        boolean res = false;
        final String sql_count = "SELECT Country FROM testcasecountry WHERE Test = ? AND TestCase = ?";
        ArrayList<String> countriesDB = new ArrayList<String>();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql_count);
            try {
                preStat.setString(1, tc.getTest());
                preStat.setString(2, tc.getTestCase());
                ResultSet rsCount = preStat.executeQuery();
                try {
                    while (rsCount.next()) {
                        countriesDB.add(rsCount.getString("Country"));
                        if (!tc.getCountryList().contains(rsCount.getString("Country"))) {
                            final String sql_delete = "DELETE FROM testcasecountry WHERE Test = ? AND TestCase = ? AND Country = ?";

                            PreparedStatement preStat2 = connection.prepareStatement(sql_delete);
                            try {
                                preStat2.setString(1, tc.getTest());
                                preStat2.setString(2, tc.getTestCase());
                                preStat2.setString(3, rsCount.getString("Country"));

                                preStat2.executeUpdate();
                            } catch (SQLException exception) {
                                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                            } finally {
                                preStat2.close();
                            }
                        }
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    rsCount.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

            res = true;
            for (int i = 0; i < tc.getCountryList().size() && res; i++) {
                if (!countriesDB.contains(tc.getCountryList().get(i))) {
                    final String sql_insert = "INSERT INTO testcasecountry (test, testcase, country) VALUES (?, ?, ?)";

                    PreparedStatement preStat2 = connection.prepareStatement(sql_insert);
                    try {
                        preStat2.setString(1, tc.getTest());
                        preStat2.setString(2, tc.getTestCase());
                        preStat2.setString(3, tc.getCountryList().get(i));

                        res = preStat2.executeUpdate() > 0;
                    } catch (SQLException exception) {
                        MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    } finally {
                        preStat2.close();
                    }
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean createTestCase(TestCase testCase) {
        boolean res = false;

        final StringBuffer sql = new StringBuffer("INSERT INTO `cerberus`.`testcase` ")
                .append(" ( `Test`, `TestCase`, `Application`, `Project`, `Ticket`, ")
                .append("`Description`, `BehaviorOrValueExpected`, ")
                .append("`ChainNumberNeeded`, `Priority`, `Status`, `TcActive`, ")
                .append("`Group`, `Origine`, `RefOrigine`, `HowTo`, `Comment`, ")
                .append("`FromBuild`, `FromRev`, `ToBuild`, `ToRev`, ")
                .append("`BugID`, `TargetBuild`, `TargetRev`, `Creator`, ")
                .append("`Implementer`, `LastModifier`, `Sla`, `function`) ")
                .append("VALUES ( ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ")
                .append("?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ")
                .append("?, ?, ? ")
                .append("); ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, testCase.getTest());
                preStat.setString(2, testCase.getTestCase());
                preStat.setString(3, testCase.getApplication());
                preStat.setString(4, testCase.getProject());
                preStat.setString(5, testCase.getTicket());
                preStat.setString(6, testCase.getShortDescription());
                preStat.setString(7, testCase.getDescription());
                preStat.setString(9, "");
                preStat.setString(9, Integer.toString(testCase.getPriority()));
                preStat.setString(10, testCase.getStatus());
                preStat.setString(11, testCase.isActive() ? "Y" : "N");
                preStat.setString(12, testCase.getGroup());
                preStat.setString(13, "");
                preStat.setString(14, "");
                preStat.setString(15, testCase.getHowTo());
                preStat.setString(16, testCase.getComment());
                preStat.setString(17, testCase.getFromSprint());
                preStat.setString(18, testCase.getFromRevision());
                preStat.setString(17, testCase.getToSprint());
                preStat.setString(19, testCase.getToRevision());
                preStat.setString(20, testCase.getBugID());
                preStat.setString(21, testCase.getTargetSprint());
                preStat.setString(22, testCase.getTargetRevision());
                preStat.setString(23, "");
                preStat.setString(24, testCase.getImplementer());
                preStat.setString(25, testCase.getLastModifier());
                preStat.setString(26, "");
                preStat.setString(27, testCase.getFunction());
                /*        preStat.setString(4, testCase.isRunQA() ? "Y" : "N");
                 preStat.setString(5, testCase.isRunUAT() ? "Y" : "N");
                 preStat.setString(6, testCase.isRunPROD() ? "Y" : "N");
                 */
                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<TCase> findTestCaseByCriteria(String test, String application, String country, String active) {
        List<TCase> list = null;
        final String query = "SELECT tc.* FROM testcase tc JOIN testcasecountry tcc "
                + "WHERE tc.test=tcc.test AND tc.testcase=tcc.testcase "
                + "AND tc.test = ? AND tc.application = ? AND tcc.country = ? AND tc.tcactive = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, application);
                preStat.setString(3, country);
                preStat.setString(4, active);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    /**
     * @since 0.9.1
     */
    @Override
    public List<TCase> findTestCaseByCriteria(TCase testCase, String text, String system) {
        List<TCase> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT t2.* FROM testcase t2 LEFT OUTER JOIN application a ON a.application=t2.application ");
        query.append(" WHERE (t2.test LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.test", testCase.getTest()));
        query.append(") AND (t2.project LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.project", testCase.getProject()));
        query.append(") AND (t2.ticket LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.ticket", testCase.getTicket()));
        query.append(") AND (t2.bugid LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.bugid", testCase.getBugID()));
        query.append(") AND (t2.origine LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.origine", testCase.getOrigin()));
        query.append(") AND (a.system LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("a.system", system));
        query.append(") AND (t2.application LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.application", testCase.getApplication()));
        query.append(") AND (t2.priority LIKE ");
        if (testCase.getPriority() != -1) {
            query.append("'");
            query.append(testCase.getPriority());
            query.append("'");
        } else {
            query.append("'%' or t2.priority is null");
        }
        query.append(") AND (t2.status LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.status", testCase.getStatus()));
        query.append(") AND (t2.group LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.group", testCase.getGroup()));
        query.append(") AND (t2.activePROD LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.activePROD", testCase.getRunPROD()));
        query.append(") AND (t2.activeUAT LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.activeUAT", testCase.getRunUAT()));
        query.append(") AND (t2.activeQA LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.activeQA", testCase.getRunQA()));
        query.append(") AND (t2.description LIKE '");
        if (text != null && !text.equalsIgnoreCase("")) {
            query.append(text);
        } else {
            query.append("%");
        }
        query.append("' OR t2.howto LIKE '");
        if (text != null && !text.equalsIgnoreCase("")) {
            query.append(text);
        } else {
            query.append("%");
        }
        query.append("' OR t2.behaviororvalueexpected LIKE '");
        if (text != null && !text.equalsIgnoreCase("")) {
            query.append(text);
        } else {
            query.append("%");
        }
        query.append("' OR t2.comment LIKE '");
        if (text != null && !text.equalsIgnoreCase("")) {
            query.append(text);
        } else {
            query.append("%");
        }
        query.append("') AND (t2.TcActive LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.TcActive", testCase.getActive()));
        query.append(") AND (t2.frombuild LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.frombuild", testCase.getFromSprint()));
        query.append(") AND (t2.fromrev LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.fromrev", testCase.getFromRevision()));
        query.append(") AND (t2.tobuild LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.tobuild", testCase.getToSprint()));
        query.append(") AND (t2.torev LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.torev", testCase.getToRevision()));
        query.append(") AND (t2.targetbuild LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.targetbuild", testCase.getTargetSprint()));
        query.append(") AND (t2.targetrev LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.targetrev", testCase.getTargetRevision()));
        query.append(") AND (t2.testcase LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.testcase", testCase.getTestCase()));
        query.append(") AND (t2.function LIKE ");
        query.append(ParameterParserUtil.wildcardOrIsNullIfEmpty("t2.function", testCase.getFunction()));
        query.append(")");
        
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadTestCaseFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    /**
     * @since 0.9.1
     */
    private TCase loadTestCaseFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("Test");
        String testCase = resultSet.getString("TestCase");
        String tcapplication = resultSet.getString("Application");
        String project = resultSet.getString("Project");
        String ticket = resultSet.getString("Ticket");
        String description = resultSet.getString("Description");
        String behavior = resultSet.getString("BehaviorOrValueExpected");
        int priority = resultSet.getInt("Priority");
        String status = resultSet.getString("Status");
        String tcactive = resultSet.getString("TcActive");
        String group = resultSet.getString("Group");
        String origin = resultSet.getString("Origine");
        String refOrigin = resultSet.getString("RefOrigine");
        String howTo = resultSet.getString("HowTo");
        String comment = resultSet.getString("Comment");
        String fromSprint = resultSet.getString("FromBuild");
        String fromRevision = resultSet.getString("FromRev");
        String toSprint = resultSet.getString("ToBuild");
        String toRevision = resultSet.getString("ToRev");
        String bugID = resultSet.getString("BugID");
        String targetSprint = resultSet.getString("TargetBuild");
        String targetRevision = resultSet.getString("TargetRev");
        String creator = resultSet.getString("Creator");
        String implementer = resultSet.getString("Implementer");
        String lastModifier = resultSet.getString("LastModifier");
        String runQA = resultSet.getString("activeQA");
        String runUAT = resultSet.getString("activeUAT");
        String runPROD = resultSet.getString("activePROD");
        String function = resultSet.getString("function");

        return factoryTestCase.create(test, testCase, origin, refOrigin, creator, implementer,
                lastModifier, project, ticket, function, tcapplication, runQA, runUAT, runPROD, priority, group,
                status, description, behavior, howTo, tcactive, fromSprint, fromRevision, toSprint,
                toRevision, status, bugID, targetSprint, targetRevision, comment, null, null, null, null);
    }

    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        List<String> list = null;
        final String query = "SELECT DISTINCT tc." + column + " FROM testcase tc ORDER BY tc." + column + " ASC";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while (resultSet.next()) {
                        list.add(resultSet.getString(1));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class
                            .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();

                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public boolean deleteTestCase(TCase testCase) {
        boolean bool = false;
        final String query = "DELETE FROM testcase WHERE test = ? and testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCase.getTest());
                preStat.setString(2, testCase.getTestCase());

                bool = preStat.executeUpdate() > 0;

            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public void updateTestCaseField(TCase tc, String columnName, String value) {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testcase set `");
        query.append(columnName);
        query.append("`=? where `test`=? and `testcase`=? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, value);
                preStat.setString(2, tc.getTest());
                preStat.setString(3, tc.getTestCase());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }

    }
}
