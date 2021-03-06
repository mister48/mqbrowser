/* Copyright (C) 2000-2009

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; version 2 of the License.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package com.qbrowser;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.AutoResizingTextArea;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideSwingUtilities;
import com.qbrowser.QBrowserV2.DeleteCleanup;
import com.qbrowser.QBrowserV2.DisplayMsgDialogRunner;
import com.qbrowser.QBrowserV2.MsgTable;
import com.qbrowser.property.Property;
import com.qbrowser.consumer.table.MessageRecordProperty;
import com.qbrowser.container.MessageContainer;
import com.qbrowser.display.DisplayDialogThreadPool;
import com.qbrowser.editor.ListCellEditor;
import com.qbrowser.icons.QBrowserIconsFactory;
import com.qbrowser.key.OSDetector;
import com.qbrowser.localstore.LocalMessageContainer;
import com.qbrowser.localstore.LocalMsgTable;
import com.qbrowser.localstore.LocalStoreManager;
import com.qbrowser.localstore.LocalStoreProperty;
import com.qbrowser.property.PropTableCellEditor;
import com.qbrowser.property.QBrowserPropertyException;
import com.qbrowser.util.QBrowserUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.jms.*;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import com.qbrowser.localstore.genericdest.LocalDestination;
import com.qbrowser.localstore.genericdest.LocalQueue;
import com.qbrowser.localstore.genericdest.LocalTopic;
import com.qbrowser.property.ActiveMQHeaderPropertyTable;
import com.qbrowser.property.ActiveMQPropertyUtil;
import com.qbrowser.property.InputProperty;
import com.qbrowser.property.PropertyInputTable;
import com.qbrowser.property.StreamMessageInputProperty;
import com.qbrowser.render.StripeTableRendererForProperty;
import com.qbrowser.tree.TreeIconPanel;
import com.qbrowser.util.ActiveMQBrowserCache;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SplashScreen;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnectionMetaData;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.broker.jmx.TopicViewMBean;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.web.DestinationFacade;
import org.apache.activemq.web.RemoteJMXBrokerFacade;
import org.apache.activemq.web.config.SystemPropertiesConfiguration;
import weblogic.utils.StringUtils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author takemura
 */
public class ActiveMQBrowser extends QBrowserV2 {


    JDialog purgeQueueDialog = null;
    public JMenuItem create_queue_item = null;
    public JMenuItem delete_queue_item = null;
    public JMenuItem create_topic_item = null;
    public JMenuItem delete_topic_item = null;
    ActiveMQBrowserCache activemqbrowsercache = null;
    ActiveMQHeaderPropertyTable amq_header_table = null;
    static final String DEFAULT_BROKER_ADMIN_ROLE = "system";
    static final String DEFAULT_BROKER_PASSWORD = "manager";
    static final String DEFAULT_JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
    public static final String QUEUE_PREFIX = "queue://";
    public static final String TOPIC_PREFIX = "topic://";
    public static String jmxUrl = DEFAULT_JMXURL;
    public static String jmxRole = DEFAULT_BROKER_ADMIN_ROLE;
    public static String jmxPassword = DEFAULT_BROKER_PASSWORD;
    public static String title = "ActiveMQBrowser " + version;
    static ArrayList    finalDestinationNames = new ArrayList();
    public static HashMap    destinationViewObjectCacheForQueue = new HashMap();
    public static HashMap    destinationViewObjectCacheForTopic = new HashMap();

    public static ResourceBundle resourcesamq = ResourceBundle.getBundle("com.qbrowser.resourceamq");
    JTextField connectiontext_jmxurl = null;
    JTextField connectiontext_role = null;
    JTextField connectiontext_conn_url = null;
    JTextField connectiontext_conn_user = null;
    JPasswordField connectiontext_conn_password = null;
    RemoteJMXBrokerFacade bf = null;

    ActiveMQBrowser() {
        super();

        
        activemqbrowsercache = new ActiveMQBrowserCache();
        popupMenuForQueueFolder.remove(create_queue_itm);
        popupMenuForTopicFolder.remove(create_topic_itm);
        popupMenuTForQueue.remove(delete_queue_itm);
        popupMenuTForTopic.remove(delete_topic_itm);
        popupMenuForBrokerFolder.remove(shutdown_bkr_itm);
        popupMenuForBrokerFolder.remove(restart_bkr_itm);

        create_queue_itm = new JMenuItem("qkey.msg.msg346",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"));
        create_queue_itm.addActionListener(new CreateQueueForTreeListener());

        create_topic_itm = new JMenuItem("qkey.msg.msg348",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"));
        create_topic_itm.addActionListener(new CreateTopicForTreeListener());

        delete_queue_itm = new JMenuItem("qkey.msg.msg356",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"));
        delete_queue_itm.addActionListener(new DeleteQueueForTreeListener());

        delete_topic_itm = new JMenuItem("qkey.msg.msg357",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"));
        delete_topic_itm.addActionListener(new DeleteTopicForTreeListener());

        shutdown_bkr_itm = new JMenuItem("qkey.msg.msg368",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Shutdown));
        shutdown_bkr_itm.addActionListener(new ShutdownBrokerListener());

        popupMenuForBrokerFolder.add(shutdown_bkr_itm);

        popupMenuForQueueFolder.add(create_queue_itm);
        popupMenuForTopicFolder.add(create_topic_itm);
        popupMenuTForQueue.remove(purgeqItemfortree);
        purgeqItemfortree = new JMenuItem("qkey.msg.msg338",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest));
        purgeqItemfortree.addActionListener(new PurgeQueueForPopupListener());
        popupMenuTForQueue.add(purgeqItemfortree);
        popupMenuTForQueue.remove(query_dest_item2);
        popupMenuTForQueue.add(delete_queue_itm);
        popupMenuTForTopic.add(delete_topic_itm);
        popupMenuTForTopic.remove(query_dest_item3);




        //宛先メニュー
        menubar.remove(destcmdmenu);
        menubar.remove(txncmdmenu);
        menubar.remove(rawcmdmenu);
        menubar.remove(cmdmenu);

        destcmdmenu = new JMenu("qkey.menu.item.destinationcmd");
     
        purge_dest_item = new JMenuItem("qkey.amq.msg.msg044",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest));
        PurgeQueueListener pdcl = new PurgeQueueListener();
        purge_dest_item.addActionListener(pdcl);

        destcmdmenu.add(purge_dest_item);

        create_queue_item = new JMenuItem("qkey.amq.msg.msg048",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"));
        create_queue_item.addActionListener(new CreateQueueListener());

        delete_queue_item = new JMenuItem("qkey.amq.msg.msg052",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"));
        delete_queue_item.addActionListener(new DeleteQueueListener());

        delete_topic_item = new JMenuItem("qkey.amq.msg.msg054",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"));
        delete_topic_item.addActionListener(new DeleteTopicListener());

        //DeleteQueueListener
        //DeleteTopicListener

        destcmdmenu.add(new JSeparator());

        destcmdmenu.add(create_queue_item);
        
        
        create_topic_item = new JMenuItem("qkey.amq.msg.msg050",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"));
        create_topic_item.addActionListener(new CreateTopicListener());

        destcmdmenu.add(create_topic_item);
        destcmdmenu.add(new JSeparator());
        destcmdmenu.add(delete_queue_item);
        destcmdmenu.add(delete_topic_item);

        //CreateTopicListener

        //CreateQueueListener

        popupMenuXForQTab.remove(purgeqItem);
        purgeqItem = new JMenuItem("qkey.msg.msg338",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest));
        purgeqItem.addActionListener(new PurgeQueueForPopupListener());
        popupMenuXForQTab.add(purgeqItem);


        menubar.add(destcmdmenu, 6);

        menu_button_container.remove(purge_atesaki_button);

        purge_atesaki_button = new JideButton(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest));
        purge_atesaki_button.setToolTipText(resourcesamq.getString("qkey.amq.msg.msg044"));
        purge_atesaki_button.addActionListener(pdcl);

        menu_button_container.add(purge_atesaki_button, 11);
        
        purge_atesaki_button.setEnabled(false);

        destcmdmenu.setEnabled(false);

        menubar.remove(versionmenu);

        versionmenu = new JMenu("qkey.menu.clientversion");
        version_item = new JMenuItem("qkey.amq.msg.mgs018",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.ClientVersion));
        version_item.addActionListener(new VersionListener());

        refresh_dest_combobox_item.addActionListener(new RefreshDestNames());

        versionmenu.add(version_item);
        menubar.add(versionmenu);

        JMenuItem aitem = addLocalstoreSubscriptionItem;

        addLocalstoreSubscriptionItem = new JMenuItem("qkey.msg.msg314",
                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Copyin));
        addLocalstoreSubscriptionItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent event) {
                                int selidx = tabbedPane.getSelectedIndex();
                                final String dest_with_suffix = tabbedPane.getTitleAt(selidx);
                                //今このローカルストアがリスンしているsuffix付宛先一覧

                                final String local_dest_without_suffix = getPureDestName(dest_with_suffix);
                                LocalStoreProperty lsp = lsm.getLocalStoreProperty(local_dest_without_suffix);

                                ArrayList listen_dests = new ArrayList();
                                Iterator ilsp = lsp.getFromDests().keySet().iterator();
                                while (ilsp.hasNext()) {
                                   MessageRecordProperty mrp = new MessageRecordProperty();
                                   String listen_dest_name_with_suffix = (String)ilsp.next();
                                   listen_dests.add(listen_dest_name_with_suffix);
                                }

                                final JComboBox rlsBox = new JComboBox();
                                 for (int i = 0; i < destinationNamesForDisplayTopic.size(); i++) {
                                    String tpcname = (String)destinationNamesForDisplayTopic.get(i);
                                    tpcname = tpcname + TOPIC_SUFFIX;
                                    if (!listen_dests.contains(tpcname)) {
                                        rlsBox.addItem(new String(tpcname));
                                    }
                                 }

                                rlsBox.setEditable(false);
                                JPanel panel = new JPanel();
                                panel.setLayout(new BorderLayout());
                                JLabel lbl = new JLabel(resources.getString("qkey.msg.msg315"));
                                panel.add(BorderLayout.NORTH, lbl);
                                panel.add(BorderLayout.CENTER, rlsBox);

                popupConfirmationDialog(resources.getString("qkey.msg.msg314"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Forward),
                        new ActionListener() {

                            public void actionPerformed(ActionEvent event) {
                                confirmDialog.dispose();
                                confirmDialog = null;

                                if (rlsBox.getItemCount() != 0) {
                                    try {
                                        lsm.addDestCopySubscriptionToLocalStore(local_dest_without_suffix, (String) rlsBox.getSelectedItem(), "");

                                        localstoreSubscriptionListItem.getActionListeners()[0].actionPerformed(event);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });



            }
        });

        popupMenuXForLSTab.add(addLocalstoreSubscriptionItem);
        popupMenuXForLSTab.remove(aitem);
        //not implemented yet.
        cmdmenu.setEnabled(false);
        
        txncmdmenu.setEnabled(false);
        rawcmdmenu.setEnabled(false);

        connection_list_button.setEnabled(false);
        services_list_button.setEnabled(false);
        services_details_button.setEnabled(false);
        broker_details_button.setEnabled(false);
        config_printer_button.setEnabled(false);
        atesaki_info_button.setEnabled(false);
        atesaki_details_button.setEnabled(false);
        all_txn_button.setEnabled(false);
        
        txn_filter_button.setEnabled(false);
        cmd_input_button.setEnabled(false);

        Dimension d = qBox.getPreferredSize();
        d.setSize((20 + d.getWidth()), d.getHeight());
        qBox.setPreferredSize(d);
        qBox.setEditable(true);

        disconnect_item.removeActionListener(disconl);
        disconnect_item.addActionListener(new DisConnectionListener());

        unsubscribe_button = null;
        unsubscribe_button = new JButton(resources.getString("qkey.menu.item.unsubscribe"));

        unsubscribe_button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                //Running→Destroyed

                //unsubscribe処理
                int sel_tab_index = tabbedPane.getSelectedIndex();
                String dispName = tabbedPane.getTitleAt(sel_tab_index);
                Boolean isRunning = (Boolean) subscribe_thread_status.get(dispName);
                JTable cTable = (JTable)jtableins.get(dispName);
                if ((isRunning != null) && isRunning.booleanValue()) {
                    SubscriberThread sthread = (SubscriberThread) subscribe_threads.get(dispName);

                       qbuttonpanel.remove(unsubscribe_button);
                       qbuttonpanel.add(BorderLayout.WEST, subscribe_resume_button);
                       qbuttonpanel.updateUI();

                       try {
                         sthread.destroy();
                       } catch (Throwable td) {
                                 //NOP
                       }
                       subscribe_thread_status.put(dispName, new Boolean(false));
                       subscribe_threads.remove(dispName);
                       String state_string = resources.getString("qkey.msg.msg137");
                       setFooter(dispName + " " + resources.getString("qkey.msg.msg074") + " " + String.valueOf(cTable.getRowCount()) + " " + state_string);

                }

                }
         });

         subscribe_resume_button = null;
         subscribe_resume_button = new JButton(resources.getString("qkey.menu.item.resumesubscribe"));
         subscribe_resume_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //Destroyed→Running
                //subscribe resume処理

                int sel_tab_index = tabbedPane.getSelectedIndex();
                String dispName = tabbedPane.getTitleAt(sel_tab_index);
                Boolean isRunning = (Boolean) subscribe_thread_status.get(dispName);
                JTable cTable = (JTable) jtableins.get(dispName);
                if ((isRunning == null) || !isRunning.booleanValue()) {

                    try {

                        createAndStartSubscriberThread(dispName);
                        qbuttonpanel.remove(subscribe_resume_button);
                        qbuttonpanel.add(BorderLayout.WEST, unsubscribe_button);
                        qbuttonpanel.updateUI();
                        String state_string = resources.getString("qkey.msg.msg136");
                        setFooter(dispName + " " + resources.getString("qkey.msg.msg074") + " " + String.valueOf(cTable.getRowCount()) + " " + state_string);

                    } catch (Throwable tex) {
                        System.err.println(tex.getMessage());
                    }

                }

            }
        });



        setFooter(resourcesamq.getString("qkey.amq.msg.msg002"));
        oya = this;
    }

    @Override
    public void initQBrowserKey() {

        ActiveMQBrowser.QBrowserKey qbk = new QBrowserKey();
        qbk.apply(this);

    }

    @Override
    public void initLocalStoreManager() {
        //ローカルストアプロパティ読込み&定義に従ってローカルストア復元
        lsm = new LocalStoreManager("amq");
        refreshLocalStoresOnMenu();
    }

    @Override
    public String getStateOfDestination(String destType, String targetname) {
          return "RUNNING";
    }


    @Override
    public void initTreePane() {
        if (treePane != null) {
          tree_location.remove(treePane);
        }

                    treePane = new TreeIconPanel(bkr_instance_name + "(" + jmxUrl + ")", oya);
                    JTree tree = treePane.getTree();
                    tree.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(final MouseEvent e) {

                            if (treePane != null) {

                                final TreeIconPanel.DestInfo di = treePane.getSelectedDestInfo();

                                if (di != null) {

                                    if (SwingUtilities.isLeftMouseButton(e)) {

                                        if (e.getClickCount() == 1) {

                                            if (!di.destinationType.equals("FD")) {
                                                qBox.setSelectedItem(di.name_with_suffix);
                                            }

                                        }
                                    } else if (SwingUtilities.isRightMouseButton(e)) {

                                        if (di.destinationType.equals("FD")) {
                                            //ツリー上の宛先ポップアップメニュー用意
                                            if (di.destinationName.equals("LocalStore")) {
                                                popupMenuForLocalStoreFolder.show(e.getComponent(), e.getX(), e.getY());
                                            } else if (di.destinationName.equals("Queue")) {
                                                popupMenuForQueueFolder.show(e.getComponent(), e.getX(), e.getY());
                                            } else if (di.destinationName.equals("Topic")) {
                                                popupMenuForTopicFolder.show(e.getComponent(), e.getX(), e.getY());
                                            }

                                        } else if (di.destinationType.equals(TOPIC_LITERAL) ||
                                                     di.destinationType.equals(CHILD_TOPIC_LITERAL)) {


                                            popupMenuTForTopic.remove(subscribe_on_tree);
                                            popupMenuTForTopic.remove(addListenToLocalStoreItem2);
                                            popupMenuTForTopic.remove(copyToLocalStoreListItem2);
                                            popupMenuTForTopic.remove(pastemsgItem4);
                                            popupMenuTForTopic.remove(topic_separator1);

                                            Boolean isRunningT = (Boolean) subscribe_thread_status.get(di.name_with_suffix);
                                            if (isRunningT == null || !isRunningT.booleanValue()) {
                                                popupMenuTForTopic.add(subscribe_on_tree);
                                            } else {
                                                popupMenuTForTopic.add(addListenToLocalStoreItem2);
                                                popupMenuTForTopic.add(topic_separator1);
                                                popupMenuTForTopic.add(copyToLocalStoreListItem2);
                                            }

                                            if (remove_child_topic_itm != null) {
                                                popupMenuTForTopic.remove(remove_child_topic_itm);
                                            }

                                            if (di.destinationType.equals(CHILD_TOPIC_LITERAL)) {
                                                remove_child_topic_itm = new JMenuItem(resources.getString("qkey.msg.msg380"),
                                                        QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));

                                                remove_child_topic_itm.addActionListener(new ActionListener() {

                                                    public void actionPerformed(ActionEvent event) {
                                                        //ターゲットのローカルストアから、この宛先を削除する
                                                        try {

                                                            String local_store_without_suffix = getPureDestName(di.parent_with_suffix);
                                                            LocalStoreProperty lsp = lsm.getLocalStoreProperty(local_store_without_suffix);


                                                            String target_dest_with_suffix = di.name_with_suffix;
                                                            lsp.removeFromDests(target_dest_with_suffix);
                                                            lsm.updateAndSaveLocalStoreProperty(lsp);
                                                            lsm.removeDestCopySubscriptionToLocalStore(local_store_without_suffix, target_dest_with_suffix);
                                                            initTreePane();

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });

                                                popupMenuTForTopic.add(remove_child_topic_itm);
                                            }

                                            if (cbm.hasClipBoardValidData()) {
                                                popupMenuTForTopic.add(pastemsgItem4);
                                            }

                                            popupMenuTForTopic.show(e.getComponent(), e.getX(), e.getY());
                                        } else if (di.destinationType.equals(QUEUE_LITERAL)) {

                                            popupMenuTForQueue.remove(pastemsgItem5);

                                            if (cbm.hasClipBoardValidData()) {
                                                popupMenuTForQueue.add(pastemsgItem5);
                                            }

                                            popupMenuTForQueue.show(e.getComponent(), e.getX(), e.getY());

                                        } else if (di.destinationType.equals(LOCAL_STORE_LITERAL) ||
                                                     di.destinationType.equals(CHILD_LOCAL_STORE_LITERAL)) {

                                            popupMenuTForLocalStore.remove(pastemsgItem6);
                                            if (remove_child_local_store_itm != null) {
                                                popupMenuTForLocalStore.remove(remove_child_local_store_itm);
                                            }

                                            if (pause_localstore_itm != null) {
                                                popupMenuTForLocalStore.remove(pause_localstore_itm);
                                            }
                                            if (resume_localstore_itm != null) {
                                                popupMenuTForLocalStore.remove(resume_localstore_itm);
                                            }

                                            final LocalStoreProperty lsp = lsm.getLocalStoreProperty(di.destinationName);
                                            if (lsp.isValid()) {
                                                pause_localstore_itm = new JMenuItem(resources.getString("qkey.msg.msg371") +
                                                        di.destinationName + resources.getString("qkey.msg.msg372"),
                                                           QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Stopped));
                                                pause_localstore_itm.addActionListener(new ActionListener() {

                                                    public void actionPerformed(ActionEvent e) {

                                                        JTextArea jta = new JTextArea("", 3, 25 + di.destinationName.length());
                                                        jta.setText(resources.getString("qkey.msg.msg374") + di.destinationName + resources.getString("qkey.msg.msg375"));
                                                        jta.setEditable(false);
                                                        jta.setBackground(Color.WHITE);

                                                        popupConfirmationDialog(resources.getString("qkey.msg.msg377"), jta,
                                                                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Confirm),
                                                                new ActionListener() {

                                                            public void actionPerformed(ActionEvent e) {
                                                                confirmDialog.dispose();
                                                                confirmDialog = null;
                                                                lsp.setValid(false);
                                                                try {
                                                                  lsm.updateAndSaveLocalStoreProperty(lsp);
                                                                } catch (Exception savee) {
                                                                    popupErrorMessageDialog(savee);
                                                                }

                                                            }

                                                        });

                                                    }
                                                });
                                                popupMenuTForLocalStore.add(pause_localstore_itm);
                                            } else {
                                                resume_localstore_itm = new JMenuItem(resources.getString("qkey.msg.msg371") +
                                                        di.destinationName + resources.getString("qkey.msg.msg373"),
                                                           QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Playing));
                                                resume_localstore_itm.addActionListener(new ActionListener() {

                                                    public void actionPerformed(ActionEvent e) {

                                                        JTextArea jta = new JTextArea("", 3, 25 + di.destinationName.length());
                                                        jta.setText(resources.getString("qkey.msg.msg374") + di.destinationName + resources.getString("qkey.msg.msg376"));
                                                        jta.setEditable(false);
                                                        jta.setBackground(Color.WHITE);

                                                        popupConfirmationDialog(resources.getString("qkey.msg.msg378"), jta,
                                                                QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Confirm),
                                                                new ActionListener() {

                                                            public void actionPerformed(ActionEvent e) {
                                                                confirmDialog.dispose();
                                                                confirmDialog = null;
                                                                lsp.setValid(true);
                                                                try {
                                                                  lsm.updateAndSaveLocalStoreProperty(lsp);
                                                                } catch (Exception savee) {
                                                                    popupErrorMessageDialog(savee);
                                                                }

                                                            }

                                                        });

                                                    }
                                                });
                                                popupMenuTForLocalStore.add(resume_localstore_itm);
                                            }

                                            if (di.destinationType.equals(CHILD_LOCAL_STORE_LITERAL)) {

                                                remove_child_local_store_itm = new JMenuItem(resources.getString("qkey.msg.msg379"),
                                                        QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));

                                                remove_child_local_store_itm.addActionListener(new ActionListener() {

                                                    public void actionPerformed(ActionEvent event) {
                                                        //ターゲットのローカルストアから、この宛先を削除する

                                                        LocalStoreProperty lsp = lsm.getLocalStoreProperty(di.destinationName);
                                                        lsp.removeFromDests(di.parent_with_suffix);
                                                        try {
                                                            lsm.updateAndSaveLocalStoreProperty(lsp);
                                                            lsm.removeDestCopySubscriptionToLocalStore(di.destinationName, di.parent_with_suffix);
                                                            initTreePane();

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });

                                                popupMenuTForLocalStore.add(remove_child_local_store_itm);
                                            }


                                            if (cbm.hasClipBoardValidData()) {
                                                popupMenuTForLocalStore.add(pastemsgItem6);
                                            }
                                            popupMenuTForLocalStore.show(e.getComponent(), e.getX(), e.getY());
                                        } else if (di.destinationType.equals("BKR")) {

                                            popupMenuForBrokerFolder.show(e.getComponent(), e.getX(), e.getY());

                                        }

                                    }
                                }
                            }

                        }
                    });
                    addDropTargetListenerToComponents(new QBrowserTreeDropTargetListener(), treePane);
                    treePane.setOpaque(true);
                    tree_location.add(treePane);
                    tree_location.updateUI();


    }


    @Override
    public void initDestListConsumer() throws JMSException {
        //リスナー定義解除
    }

    @Override
    void setConnected() {
        purge_atesaki_button.setEnabled(true);
        destcmdmenu.setEnabled(true);

        newmmenu.setEnabled(true);
        editmenu.setEnabled(true);
        displaymenu.setEnabled(true);
        openmessage_item.setEnabled(true);
        open_multimessage_item.setEnabled(true);
        localstoremenu.setEnabled(true);
        subscribemenu.setEnabled(true);

        qBrowse.setEnabled(true);
        qSearch.setEnabled(true);
        qBox.setEnabled(true);

        new_button.setEnabled(true);
        new_buttonf.setEnabled(true);
        open_message_button.setEnabled(true);
        open_multi_message_button.setEnabled(true);
        createlocalstore_button.setEnabled(true);
        select_all_button.setEnabled(true);
        subscribe_button.setEnabled(true);
        delete_button.setEnabled(true);
        search_button.setEnabled(true);
        disconnect_item.setEnabled(true);

        delete.setEnabled(true);
        details.setEnabled(true);


    }

    @Override
    void createAndStartSubscriberThread(String compl_subscribename) {
        //System.out.println("QBrowserV2ForWLMQ::createAndStartSubscriberThread called.");
        SubscriberRunner srun = new SubscriberRunner();
        srun.dest_full_name = compl_subscribename;

        SubscriberThread sth = new SubscriberThread(srun);
        sth.start();

        subscribe_thread_status.put(compl_subscribename, new Boolean(true));
        subscribe_threads.put(compl_subscribename, sth);
    }

    @Override
    void restartSubscriberThreadAlongWithCurrentStatus(String compl_subscribename) {
              boolean cu_status = isSubscriberThreadRunning(compl_subscribename);
              stopSubscriberThread(compl_subscribename);
              //今現在購読中の場合のみスレッドを再作成
              if (cu_status)
              createAndStartSubscriberThread(compl_subscribename);
    }

    public Destination getRealDestinationFromIfLocalDestination(Destination dest) throws Exception {
                //とりあえずローカル宛先オブジェクトの形で復元されている宛先を
                //本物に取り替える
                if (dest instanceof com.qbrowser.localstore.genericdest.LocalQueue) {
                    return session.createQueue(((com.qbrowser.localstore.genericdest.LocalQueue)dest).getQueueName());

                } else if (dest instanceof com.qbrowser.localstore.genericdest.LocalTopic) {
                    return session.createTopic(((com.qbrowser.localstore.genericdest.LocalTopic)dest).getTopicName());
                } else {
                    return dest;
                }
    }

    public void copyMessageHeaders(ArrayList headers, Message destmsg) throws JMSException {

            for (int i = 0; i < headers.size(); i++) {

                try {
                Property prop = (Property)headers.get(i);
                String key = prop.getKey();
                if (key.equals("JMSMessageID")) {
                   destmsg.setJMSMessageID(prop.getProperty_valueASString());
                } else if (key.equals("JMSDestination")) {


                    final String destname = prop.getProperty_valueASString();

                    if (destname != null && destname.length() > 0) {


                    if (isTopic(destname) || destname.trim().toLowerCase().startsWith("topic://")) {

                        String cjp = getPureDestName(destname);
                        Destination ttdest = session.createTopic(cjp);
                        destmsg.setJMSDestination(ttdest);

                    } else {
                        String cjp = getPureDestName(destname);
                        Destination ttdest = session.createQueue(cjp);
                        destmsg.setJMSDestination(ttdest);
                    }


                   }

                } else if (key.equals("JMSReplyTo")) {

                    final String destname = prop.getProperty_valueASString();

                    if (destname != null && destname.length() > 0) {

                    if (isTopic(destname) || destname.trim().toLowerCase().startsWith("topic://")) {


                        String cjp = getPureDestName(destname);
                        Destination ttdest = session.createTopic(cjp);
                        destmsg.setJMSReplyTo(ttdest);

                    } else {
                        String cjp = getPureDestName(destname);
                        Destination ttdest = session.createQueue(cjp);
                        destmsg.setJMSReplyTo(ttdest);
                    }

                    }


                 } else if (key.equals("JMSCorrelationID")) {

                     String colid = prop.getProperty_valueASString();

                     if ((colid != null) && (colid.length() > 0) && (!colid.equals("null"))) {
                       destmsg.setJMSCorrelationID(colid);
                     }

                 } else if (key.equals("JMSDeliverMode")) {

                     destmsg.setJMSDeliveryMode(prop.getProperty_valueASInt());

                 } else if (key.equals("JMSPriority")) {

                     destmsg.setJMSPriority(prop.getProperty_valueASInt());

                 } else if (key.equals("JMSExpiration")) {

                     destmsg.setJMSExpiration(prop.getProperty_valueASLong());

                 } else if (key.equals("JMSType")) {

                     String jms_type = prop.getProperty_valueASString();

                     if ((jms_type != null) && (jms_type.length() > 0) && (!jms_type.equals("null"))) {
                       destmsg.setJMSType(jms_type);
                     }

                 } else if (key.equals("JMSRedelivered")) {

                     destmsg.setJMSRedelivered(prop.getProperty_valueASBoolean());

                 } else if (key.equals("JMSTimestamp")) {
                     destmsg.setJMSTimestamp(prop.getProperty_valueASLong());

                 } else if (key.equalsIgnoreCase("Message Group Sequence Number")) {

                    org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)destmsg;
                    acm.setGroupSequence(prop.getProperty_valueASInt());

                 } else if (key.equalsIgnoreCase("Message Group")) {

                    org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)destmsg;
                    acm.setGroupID(prop.getProperty_valueASString());

                }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

    }

    @Override
    Message copyMessage(Message srcmsg) throws JMSException {

        if (srcmsg == null) return null;

        Message copyresultmsg = null;

        //タイプ別に応じてメッセージフレーム作成とボディ特有データコピー
        if (srcmsg instanceof TextMessage) {

           TextMessage txtmsg = session.createTextMessage();
           TextMessage fromtxtmsg = (TextMessage)srcmsg;
           txtmsg.setText(fromtxtmsg.getText());
           copyresultmsg = txtmsg;

        } else if (srcmsg instanceof BytesMessage) {

           BytesMessage bmsg = session.createBytesMessage();
           BytesMessage frombmsg = (BytesMessage)srcmsg;
           frombmsg.reset();

           byte[] bibi = new byte[1024];
           int len = 0;

           while ((len = frombmsg.readBytes(bibi)) != -1) {
                        bmsg.writeBytes(bibi, 0, len);
           }

           copyresultmsg = bmsg;

        } else if (srcmsg instanceof MapMessage) {

           MapMessage mmsg = session.createMapMessage();
           MapMessage frommmsg = (MapMessage)srcmsg;

           for (Enumeration enu = frommmsg.getMapNames();
                    enu.hasMoreElements();) {
                String name = (enu.nextElement()).toString();
                Object obj = frommmsg.getObject(name);

                if (obj instanceof String) {

                      mmsg.setString(name, (String)obj);

                } else if (obj instanceof Integer) {

                      mmsg.setInt(name, (Integer)obj);

                } else if (obj instanceof Boolean) {

                      mmsg.setBoolean(name, (Boolean)obj);

                } else if (obj instanceof Byte) {

                      mmsg.setByte(name, (Byte)obj);

                } else if (obj instanceof Double) {

                      mmsg.setDouble(name, (Double)obj);

                } else if (obj instanceof Float) {

                      mmsg.setFloat(name, (Float)obj);

                } else if (obj instanceof Long) {

                      mmsg.setLong(name, (Long)obj);

                } else if (obj instanceof Short) {

                      mmsg.setShort(name, (Short)obj);

                } else if (obj instanceof byte[]) {

                      mmsg.setBytes(name, (byte[])obj);

                } else {
                      mmsg.setObject(name, obj);
                }

            }

            copyresultmsg = mmsg;


        } else if (srcmsg instanceof StreamMessage) {

           StreamMessage smsg = session.createStreamMessage();
           StreamMessage fromsmsg = (StreamMessage)srcmsg;
           fromsmsg.reset();

                Object ro = null;
                try {
                while ((ro = ((StreamMessage)fromsmsg).readObject()) != null) {

                    if (ro instanceof Boolean) {

                       smsg.writeBoolean(((Boolean)ro).booleanValue());

                    } else if (ro instanceof Byte) {

                       smsg.writeByte(((Byte)ro).byteValue());

                    } else if (ro instanceof Short) {

                       smsg.writeShort(((Short)ro).shortValue());

                    } else if (ro instanceof java.lang.Character) {

                       smsg.writeChar(((Character)ro).charValue());

                    } else if (ro instanceof java.lang.Integer) {

                      smsg.writeInt(((Integer)ro).intValue());

                    } else if (ro instanceof java.lang.Long) {

                      smsg.writeLong(((Long)ro).longValue());

                    } else if (ro instanceof java.lang.Float) {

                      smsg.writeFloat(((Float)ro).floatValue());

                    } else if (ro instanceof java.lang.Double) {

                      smsg.writeDouble(((Double)ro).doubleValue());

                    } else if (ro instanceof java.lang.String) {

                      smsg.writeString((String)ro);

                    } else if (ro instanceof byte[]) {

                        byte[] bytearray = (byte[])ro;
                        smsg.writeBytes(bytearray);

                    }
                }
                } catch (MessageEOFException eof) {
                    //NOP
                } catch (Throwable thex) {
                    //NOP
                }

           copyresultmsg = smsg;

        } else if (srcmsg instanceof ObjectMessage) {

           ObjectMessage omsg = session.createObjectMessage();
           ObjectMessage fromomsg = (ObjectMessage)srcmsg;
           omsg.setObject(fromomsg.getObject());

           copyresultmsg = omsg;

        } else if (srcmsg instanceof Message) {
            Message omsg = session.createMessage();
            copyresultmsg = omsg;
        }

        if (copyresultmsg != null) {

            //ヘッダ情報コピー
            QBrowserUtil.copyMessageHeaders(srcmsg, copyresultmsg);
            org.apache.activemq.command.Message acm_from = (org.apache.activemq.command.Message)srcmsg;
            org.apache.activemq.command.Message acm_to = (org.apache.activemq.command.Message)copyresultmsg;
            acm_to.setGroupID(acm_from.getGroupID());
            acm_to.setGroupSequence(acm_from.getGroupSequence());
            
            //ユーザプロパティコピー
            QBrowserUtil.copyUserProperties(srcmsg, copyresultmsg);

        }


        return copyresultmsg;
    }

  public static void populateHeadersOfLocalMessageContainer(ArrayList headers, LocalMessageContainer destmsg) throws JMSException {

            for (int i = 0; i < headers.size(); i++) {

                try {
                Property prop = (Property)headers.get(i);
                String key = prop.getKey();
                if (key.equals("JMSMessageID")) {
                   destmsg.setVmsgid(prop.getProperty_valueASString());
                } else if (key.equals("JMSDestination")) {


                    final String destname = prop.getProperty_valueASString();

                    if (destname != null && destname.length() > 0) {

                    if ((destname.toLowerCase().indexOf(QBrowserUtil.TOPIC_SUFFIX_LOWER) != -1) || destname.trim().toLowerCase().startsWith(QBrowserUtil.TOPIC_PREFIX_LOWER)) {
                        LocalTopic tp = new LocalTopic(QBrowserUtil.getPureDestName(destname));
                        tp.setOriginalDestinationWithSuffix(destname);
                        destmsg.setVdest(tp);


                    } else {
                        LocalQueue lq = new LocalQueue(QBrowserUtil.getPureDestName(destname));
                        lq.setOriginalDestinationWithSuffix(destname);
                        destmsg.setVdest(lq);
                    }

                   }

                } else if (key.equals("JMSReplyTo")) {

                    final String destname = prop.getProperty_valueASString();

                    if (destname != null && destname.length() > 0) {

                    if ((destname.toLowerCase().indexOf(QBrowserUtil.TOPIC_SUFFIX_LOWER) != -1) || destname.trim().toLowerCase().startsWith(QBrowserUtil.TOPIC_PREFIX_LOWER)) {
                        LocalTopic tp = new LocalTopic(QBrowserUtil.getPureDestName(destname));
                        tp.setOriginalDestinationWithSuffix(destname);
                        destmsg.setVreplyto(tp);

                    } else {
                        LocalQueue lq = new LocalQueue(QBrowserUtil.getPureDestName(destname));
                        lq.setOriginalDestinationWithSuffix(destname);
                        destmsg.setVreplyto(lq);
                    }

                    }


                 } else if (key.equals("JMSCorrelationID")) {

                     String colid = prop.getProperty_valueASString();

                     if ((colid != null) && (colid.length() > 0) && (!colid.equals("null"))) {
                       destmsg.setVcorrelationid(colid);
                     }

                 } else if (key.equals("JMSDeliverMode")) {

                     destmsg.setVdeliverymode(prop.getProperty_valueASInt());

                 } else if (key.equals("JMSPriority")) {

                     destmsg.setVpriority(prop.getProperty_valueASInt());

                 } else if (key.equals("JMSExpiration")) {

                     destmsg.setVexpiration(prop.getProperty_valueASLong());

                 } else if (key.equals("JMSType")) {

                     String jms_type = prop.getProperty_valueASString();

                     if ((jms_type != null) && (jms_type.length() > 0) && (!jms_type.equals("null"))) {
                       destmsg.setVjms_type(jms_type);
                     }

                 } else if (key.equals("JMSRedelivered")) {

                     destmsg.setVredelivered(prop.getProperty_valueASBoolean());

                 } else if (key.equals("JMSTimestamp")) {
                     destmsg.setVtimestamp(prop.getProperty_valueASLong());

                 }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

    }



    @Override
    void initJMS() throws JMSException {

        String user = connectiontext_conn_user.getText();
        String password = new String(connectiontext_conn_password.getPassword());
        String url = connectiontext_conn_url.getText();

        try {

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
        connection = connectionFactory.createConnection();
        connection.start();
        
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        finalDestinationNames = new ArrayList();
        destinationViewObjectCacheForQueue = new HashMap();
        destinationViewObjectCacheForTopic = new HashMap();
        destinationNamesForDisplayQueue = new ArrayList();
        destinationNamesForDisplayTopic = new ArrayList();

        collectDestination();

        } catch (Throwable tt) {
            System.err.println("initJMS : " + tt.getMessage());
        }

    }

    @Override
    public void doBrowse() {

        ComboBoxEditor editor = qBox.getEditor();

        String dispName = complementDestName((String) editor.getItem());
        setFooter(dispName + resources.getString("qkey.msg.msg073"));

        int current_tab_index = 0;

        if(!isNamedTabAlreadyCreated(dispName)) {
           current_tab_index = createNewMsgPane(dispName);

        } else {
           current_tab_index = tabbedPane.indexOfTab(dispName);
           tabbedPane.setSelectedIndex(current_tab_index);

        }

        JTable cTable = (JTable)jtableins.get(dispName);
        String name = getPureDestName(dispName);

        // Browse queue
        try {

            //キャッシュから
            Queue q = session.createQueue(name);

            QueueBrowser qb;
            if (selector == null) {
                qb = session.createBrowser(q);
            } else {
                qb = session.createBrowser(q, selector);
                //セレクタ文字列は検索ごとにリセット
                selector = null;
           }
            // Load messages into table
            MsgTable mt = (MsgTable) cTable.getModel();
            Enumeration emt = qb.getEnumeration();
            ArrayList tc = new ArrayList();
            while (emt.hasMoreElements()) {
                Message imsg = (Message)emt.nextElement();
                MessageContainer mc = new MessageContainer();
                //mc.setMessage(imsg);
                mc.setMessageFromBrokerWithLazyLoad(imsg);
                mc.setDest_name_with_suffix(dispName);

                //追加ヘッダ属性セット
                ArrayList additional = new ArrayList();
                org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)imsg;

                String jgroupid = acm.getGroupID();
                if ((jgroupid != null) && (jgroupid.length() != 0)) {
                  Property jgroupidp = new Property();
                  jgroupidp.setKey("Message Group");
                  jgroupidp.setProperty_type(Property.STRING_TYPE);
                  jgroupidp.setProperty_value(jgroupid);
                  additional.add(jgroupidp);
                }

                int jgsq = acm.getGroupSequence();

                if (jgsq != 0) {
                  Property jgsqp = new Property();
                  jgsqp.setKey("Message Group Sequence Number");
                  jgsqp.setProperty_type(Property.INT_TYPE);
                  jgsqp.setProperty_value(jgsq);
                  additional.add(jgsqp);
                }

                mc.setAdditionalHeaders(additional);


                try {
                 mc.setVdest(convertVendorDestinationToLocalDestination(imsg.getJMSDestination()));
                 mc.setVreplyto(convertVendorDestinationToLocalDestination(imsg.getJMSReplyTo()));
                } catch (Exception mce) { mce.printStackTrace();}

                tc.add(mc);
                imsg = null;
            }
            int n = mt.load(tc);

            setFooter(name + " " + resources.getString("qkey.msg.msg074") + " " + String.valueOf(n) + " " + resources.getString("qkey.msg.msg075"));
            qb.close();
        } catch (JMSException ex) {
            setFooter(ex.getMessage());
            //接続ロスト時自動回復

                try {
                  reconnect();
                  doBrowse();
                } catch (Exception recex) {
                    //popupErrorMessageDialog(recex);
                    System.err.println(recex.getMessage());
                }

                setFooter(resourcesamq.getString("qkey.amq.msg.mgs019"));

        }


        reNumberCTable(cTable);

    }


    @Override
    public void doBrowse(int tabindex) {

        //-1の時は出さない
        if (tabindex != -1) {

        String dispName = tabbedPane.getTitleAt(tabindex);
        String name = getPureDestName(dispName);
        setFooter(name + resources.getString("qkey.msg.msg073"));

        JTable cTable = (JTable)jtableins.get(dispName);

        // Browse queue
        try {
            //キャッシュから
            Queue q = session.createQueue(name);

            QueueBrowser qb;
            if (selector == null) {
                qb = session.createBrowser(q);
            } else {

                qb = session.createBrowser(q, selector);

                //セレクタ文字列は検索ごとにリセット
                selector = null;
            }
            // Load messages into table
            MsgTable mt = (MsgTable) cTable.getModel();
            Enumeration emt = qb.getEnumeration();
            ArrayList tc = new ArrayList();
            while (emt.hasMoreElements()) {
                Message imsg = (Message)emt.nextElement();
                MessageContainer mc = new MessageContainer();
                //mc.setMessage(imsg);
                mc.setMessageFromBrokerWithLazyLoad(imsg);
                mc.setDest_name_with_suffix(dispName);

                //追加ヘッダ属性セット
                ArrayList additional = new ArrayList();
                org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)imsg;

                String jgroupid = acm.getGroupID();
                if ((jgroupid != null) && (jgroupid.length() != 0)) {
                  Property jgroupidp = new Property();
                  jgroupidp.setKey("Message Group");
                  jgroupidp.setProperty_type(Property.STRING_TYPE);
                  jgroupidp.setProperty_value(jgroupid);
                  additional.add(jgroupidp);
                }

                int jgsq = acm.getGroupSequence();

                if (jgsq != 0) {
                  Property jgsqp = new Property();
                  jgsqp.setKey("Message Group Sequence Number");
                  jgsqp.setProperty_type(Property.INT_TYPE);
                  jgsqp.setProperty_value(jgsq);
                  additional.add(jgsqp);
                }

                mc.setAdditionalHeaders(additional);

                try {
                 mc.setVdest(convertVendorDestinationToLocalDestination(imsg.getJMSDestination()));
                 mc.setVreplyto(convertVendorDestinationToLocalDestination(imsg.getJMSReplyTo()));
                } catch (Exception mce) { mce.printStackTrace();}

                tc.add(mc);
                imsg = null;
            }
            int n = mt.load(tc);

            tabbedPane.setSelectedIndex(tabindex);
            setFooter(name + " " + resources.getString("qkey.msg.msg074") + " " + String.valueOf(n) + " " + resources.getString("qkey.msg.msg075"));
            qb.close();
        } catch (JMSException ex) {
           setFooter(ex.getMessage());
            //接続ロスト時自動回復

                try {
                  reconnect();
                  doBrowse();
                } catch (Exception recex) {
                    popupErrorMessageDialog(recex);
                    //System.err.println(recex.getMessage());
                }

                setFooter(resourcesamq.getString("qkey.amq.msg.mgs019"));



        }

                reNumberCTable(cTable);

        }

    }


    @Override
    public void showConnectionWindow() {

        // Create popup
        if (connectionDialog == null) {
            connectionDialog = new JDialog();
            connectionDialog.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Connect).getImage());
            connectionDialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    //
                }
            });

            connectionDialog.setLocation(380, 95);
            connectionDialog.getContentPane().setLayout(new BorderLayout());

            connectionmsgPanel = new JPanel();
            connectionmsgPanel.setLayout(new BorderLayout());

            connectionDialog.setSize(200, 200);
            connectionDialog.setTitle(resources.getString("qkey.msg.msg166"));

            connectiontext_jmxurl = new JTextField(30);
            connectiontext_role = new JTextField(12);
            connectiontext_password = new JPasswordField(12);

            JLabel connectionlabel = new JLabel(resources.getString("qkey.msg.msg168"));
            JLabel connectionlabel2 = new JLabel(resources.getString("qkey.msg.msg169"));
            JPanel expl = new JPanel();
            expl.setLayout(new BorderLayout());


            expl.add(BorderLayout.CENTER, connectionlabel);
            expl.add(BorderLayout.SOUTH, connectionlabel2);

            //テンプレート
            connectionTemplateBox = new JComboBox();
            connectionTemplateBox.setSize(200, 30);
            connectionTemplateBox.addItemListener(new ConnectionTemplateItemListener());
            ArrayList extracted_history = QBrowserUtil.getHistoryFromFile("connect_history_amq");
            QBrowserUtil.ArrayListToJComboBox(extracted_history, connectionTemplateBox);



            //過去一回もやったことのない場合もしくは履歴ファイルがない場合はデフォルトで１つ追加
            if (extracted_history.size() == 0) {
              DefaultComboBoxModel model = (DefaultComboBoxModel) connectionTemplateBox.getModel();
              model.addElement("jmxUrl = service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi role = system password = manager");
            }

            JPanel con_panel = new JPanel();


            GridBagLayout gbag = new GridBagLayout();
            con_panel.setLayout(gbag);
            GridBagConstraints vcs = new GridBagConstraints();
            connectiontext_conn_url = new JTextField(30);
            connectiontext_conn_url.setText(ActiveMQConnection.DEFAULT_BROKER_URL);
            connectiontext_conn_user = new JTextField(13);
            connectiontext_conn_user.setText(ActiveMQConnection.DEFAULT_USER);
            connectiontext_conn_password = new JPasswordField(13);
            connectiontext_conn_password.setText(ActiveMQConnection.DEFAULT_PASSWORD);

            int countY = 0;
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resourcesamq.getString("qkey.amq.msg.mgs003") + "  ", connectiontext_jmxurl, countY++);
            QBrowserUtil.addBlankRow(vcs, con_panel, gbag, countY++);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resourcesamq.getString("qkey.amq.msg.mgs004") + "  ", connectiontext_role, countY++);
            QBrowserUtil.addBlankRow(vcs, con_panel, gbag, countY++);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resourcesamq.getString("qkey.amq.msg.mgs005") + "  ", connectiontext_password, countY++);
            QBrowserUtil.addBlankRow(vcs, con_panel, gbag, countY++);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resourcesamq.getString("qkey.amq.msg.mgs011") + "  ", connectiontext_conn_url, countY++);
            QBrowserUtil.addBlankRow(vcs, con_panel, gbag, countY++);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resourcesamq.getString("qkey.amq.msg.mgs012") + "  ", connectiontext_conn_user, countY++);
            QBrowserUtil.addBlankRow(vcs, con_panel, gbag, countY++);
            QBrowserUtil.addLabelAndValueComponent(vcs, con_panel, gbag, resourcesamq.getString("qkey.amq.msg.mgs013") + "  ", connectiontext_conn_password, countY++);

            /*
             * failover://tcp://localhost:61616
             */

            connectionmsgPanel.add(BorderLayout.NORTH, expl);
            connectionmsgPanel.add(BorderLayout.CENTER, con_panel);
            JButton okbutton1 = new JButton("              OK              ");
            okbutton1.addActionListener(new ConnectionOKListener());
            JButton cancelbutton = new JButton("         " + resources.getString("qkey.msg.msg035") + "             ");
            cancelbutton.addActionListener(new ConnectionCancelListener());

            JPanel pbuttonpanel = new JPanel();
            pbuttonpanel.setLayout(new BorderLayout());
            pbuttonpanel.add(BorderLayout.WEST, okbutton1);
            pbuttonpanel.add(BorderLayout.CENTER, cancelbutton);

            JPanel temppanel = new JPanel();
            temppanel.setLayout(new BorderLayout());
            JLabel templabel = new JLabel(resources.getString("qkey.msg.msg167"));

            temppanel.add(BorderLayout.NORTH, templabel);
            temppanel.add(BorderLayout.CENTER, connectionTemplateBox);
            temppanel.add(BorderLayout.SOUTH, pbuttonpanel);

            connectionmsgPanel.add(BorderLayout.SOUTH, temppanel);
            connectionDialog.getContentPane().add(BorderLayout.NORTH, connectionmsgPanel);
            connectionDialog.setLocation(oya_frame.getX() + 280, oya_frame.getY() + 230);
            connectionDialog.pack();

        }

        connectionDialog.setVisible(true);



    }

    @Override
    void showDeleteConfirmation(int[] rows) {
        // Create popup
        if (deleteconfirmDialog != null && deleteconfirmDialog.isShowing()) {
            deleteconfirmDialog.dispose();
        }
        deleteconfirmDialog = new JDialog();
        deleteconfirmDialog.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest).getImage());

        deleteconfirmDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                currentDeleteTarget.clear();
            }
        });

        deleteconfirmDialog.setLocation(120,120);
        deleteconfirmDialog.getContentPane().setLayout(new BorderLayout());

        msgPanel = new JPanel();
        msgPanel.setLayout(new BorderLayout());
        delmsg = new JPanel();
        deleteconfirmDialog.setSize(700, 100);
        deleteconfirmDialog.setTitle(resources.getString("qkey.msg.msg007"));


        //TextArea ta = new TextArea("", 50, 33, TextArea.SCROLLBARS_VERTICAL_ONLY);
        AutoResizingTextArea ta = new AutoResizingTextArea(3,25,35);

        ta.setEditable(false);

        int tabindex = tabbedPane.getSelectedIndex();
        String tkey = tabbedPane.getTitleAt(tabindex);
        JTable cTable = (JTable)jtableins.get(tkey);

        MsgTable mt = (MsgTable) cTable.getModel();


        StringBuilder mediumbuffer = new StringBuilder();


        for (int i = 0; i < rows.length; i++) {
            try {
                SimpleDateFormat df =
                        new SimpleDateFormat("yyyy/MM/dd:kk:mm:ss z");

                MessageContainer msg = mt.getMessageAtRow(rows[i]);

                mediumbuffer.append("MsgID = " + msg.getVmsgid() + "\n");
                currentDeleteTarget.add(msg);
                //
            } catch (Exception messagee) {
                //NOP
                //messagee.printStackTrace();
            }

        }

        ta.append(mediumbuffer.toString());

        delmsg.add(new JScrollPane(ta));
        msgPanel.add(BorderLayout.NORTH, delmsg);
        del_okbutton1 = new JButton("                  OK                  ");
        del_okbutton1.addActionListener(new DeleteOKListener());
        JButton cancelbutton = new JButton(resources.getString("qkey.msg.msg008"));
        cancelbutton.addActionListener(new DeleteCancelListener());


        JPanel pbuttonpanel = new JPanel();
        pbuttonpanel.setLayout(new BorderLayout());
        pbuttonpanel.add(BorderLayout.WEST, del_okbutton1);
        pbuttonpanel.add(BorderLayout.CENTER, cancelbutton);

        msgPanel.add(BorderLayout.SOUTH, pbuttonpanel);


        deleteconfirmDialog.getContentPane().add(BorderLayout.NORTH, msgPanel);
        deleteconfirmDialog.pack();
        deleteconfirmDialog.setLocationRelativeTo(oya);

        deleteconfirmDialog.setVisible(true);

        ta.append(resources.getString("qkey.msg.msg009"));

    }


    @Override
    void showNewMessagePanel(boolean cleanupmode) {

        //cleanupモードでは、外側のフレームは残して中を一新する
        //newmessageFrame == nullの時は完全新規作成（再利用なし）
        //newmessageFrame != null で !cleanupmodeのときは、作成を全部飛ばす


        if (newmessageFrame != null && !cleanupmode) {
        } else {

            if (newmessageFrame == null) {
                // Create popup
                newmessageFrame = new JFrame();
                newmessageFrame.setTitle(resourcesamq.getString("qkey.amq.msg.mgs021"));
                newmessageFrame.setBackground(Color.white);
                newmessageFrame.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg).getImage());
                newmessageFrame.getContentPane().setLayout(new BorderLayout());

            }

            if (cleanupmode) {
                JPanel newcp = new JPanel();
                newcp.setLayout(new BorderLayout());
                newmessageFrame.setContentPane(newcp);

            }

            newmessageFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    if (stringEditDialog != null) {
                        stringEditDialog.dispose();
                        stringEditDialog = null;
                    }
                }
            });

            JPanel northpanel = new JPanel();
            northpanel.setLayout(new BorderLayout());

            //宛先入力はコンボボックスに変更
            matesakiBox1 = new JComboBox();

            Dimension dm = matesakiBox1.getPreferredSize();
            dm.setSize(10 * dm.getWidth(), dm.getHeight());
            matesakiBox1.setPreferredSize(dm);
            matesakiBox1.setEditable(false);

            
            //ヘッダパネル
            header_table = new ActiveMQHeaderPropertyTable(0);
            hTable = new JTable(header_table);


            hTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            hTable.setDefaultRenderer(Object.class, new StripeTableRendererForProperty());

            //TODO プロパティの多さによって最大200くらいまで動的拡張するようにする
            hTable.setPreferredScrollableViewportSize(new Dimension(500,100));

            DefaultCellEditor hdce = new DefaultCellEditor(getHeaderPropTypeComboBox());
            //ActiveMQ用プロパティ追加
            hptc.addItem("Message Group Sequence Number");
            hptc.addItem("Message Group");
            TableColumn hcolumn = hTable.getColumnModel().getColumn(0);
            hdce.setClickCountToStart(0);
            hcolumn.setCellEditor(hdce);

            hdce2 = new PropTableCellEditor();
            TableColumn hcolumn2 = hTable.getColumnModel().getColumn(1);
            hdce2.setClickCountToStart(0);
            hdce2.addCellEditorListener(new CellEditorListener() {

               public void editingStopped(ChangeEvent e) {
                    //今表の中にある全部の行をvalidateする

                    try {

                         //重複チェック用
                        HashSet keycheck = new HashSet();

                        for (int hi = 0; hi < header_table.getRowCount(); hi++) {
                            Property hpr = header_table.getPropertyAtRow(hi);
                            String key = hpr.getKey();
                            Object val = hpr.getProperty_value();

                            if (key != null) {
                            if (keycheck.contains(key)) {
                                throw new QBrowserPropertyException("Q0020" + MAGIC_SEPARATOR + key + MAGIC_SEPARATOR + hpr.getProperty_type() + MAGIC_SEPARATOR + val);
                            } else {
                                //System.out.println("abc");
                                keycheck.add(key);
                            }
                            }


                            ActiveMQPropertyUtil.validateJMSHeaderValueType(key, val);

                        }
                        newmessage1stpanelok = true;
                    } catch (QBrowserPropertyException qpe) {
                        //cmessagefooter.setText(qpe.getMessage());

                        last_jmsheader_validate_error = qpe.getMessage();

                        newmessage1stpanelok = false;
                    }

                }

                public void editingCanceled(ChangeEvent e) {}
            });

            hcolumn2.setCellEditor(hdce2);

            JScrollPane htablePane = new JScrollPane(hTable);
            JPanel hp = new JPanel();
            hp.setLayout(new BorderLayout());

            //プロパティヘッダパネル
            JPanel header_header_container = new JPanel();
            header_header_container.setLayout(new BorderLayout());

            JLabel header_header_label = new JLabel(resources.getString("qkey.msg.msg024"));
            JPanel hbutton_container = new JPanel();
            JButton hplus_button = new JButton("+");
            hplus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    header_table.add_one_empty_row();
            }

            });

            JButton hminus_button = new JButton("-");
            hminus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    int sel_row = hTable.getSelectedRow();
                    if (sel_row < 0) sel_row = 0;

                    if (header_table.getRowCount() > 0)
                    header_table.deletePropertyAtRow(sel_row);

            }

            });
            hbutton_container.add(hplus_button);
            hbutton_container.add(hminus_button);

            header_header_container.setBorder(BorderFactory.createEtchedBorder());

            header_header_container.add(header_header_label, BorderLayout.CENTER);
            header_header_container.add(hbutton_container, BorderLayout.EAST);

            hp.setBorder(BorderFactory.createEtchedBorder());

            hp.add(header_header_container, BorderLayout.NORTH);
            hp.add(htablePane, BorderLayout.CENTER);


            northpanel.add(BorderLayout.SOUTH, hp);

            mqBox = new JComboBox();
            mqBox.addItemListener(new SendAtesakiComboBoxItemListener());
            Dimension d = mqBox.getPreferredSize();
            d.setSize(10 * d.getWidth(), d.getHeight());
            mqBox.setPreferredSize(d);
            mqBox.setEditable(false);

            DefaultComboBoxModel model = (DefaultComboBoxModel) mqBox.getModel();
            model.addElement(QUEUE_LITERAL);
            model.addElement(TOPIC_LITERAL);
            model.addElement(LOCAL_STORE_LITERAL);

            JLabel jl01 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs022"));
            northpanel.add(BorderLayout.WEST, jl01);
            northpanel.add(BorderLayout.EAST, mqBox);

            //宛先名入力エリア
            JPanel atesaki = new JPanel();
            atesaki.setLayout(new BorderLayout());
            JLabel jl02 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs023"));
            atesaki.add(BorderLayout.WEST, jl02);

            //データ入れ込み。デフォルトはQUEUE
            importQueueNamesToMATESAKIBOX1();
            matesakiBox1.setEditable(true);

            atesaki.add(BorderLayout.CENTER, matesakiBox1);

            northpanel.add(BorderLayout.NORTH, atesaki);

            newmessageFrame.getContentPane().add(BorderLayout.NORTH, northpanel);

            //mpropertyPanel = new PropertyPanel();
            //mpropertyPanel.setTitle(resources.getString("qkey.msg.msg158"));
            //newmessageFrame.getContentPane().add(BorderLayout.CENTER, mpropertyPanel);
            property_table = new PropertyInputTable(0);

            //newmessageFrame.getContentPane().add(BorderLayout.CENTER, mpropertyPanel);
            pTable = new JTable(property_table);
            pTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            pTable.setDefaultRenderer(Object.class, new StripeTableRendererForProperty());
            pTable.setRowHeight(20);
            pTable.setColumnSelectionAllowed(false);

            //TODO プロパティの多さによって最大200くらいまで動的拡張するようにする
            pTable.setPreferredScrollableViewportSize(new Dimension(500,120));

            TableColumn column0 = pTable.getColumnModel().getColumn(0);
            pdce1 = new DefaultCellEditor(new JTextField());
            pdce1.setClickCountToStart(0);
            column0.setCellEditor(pdce1);

            TableColumn column = pTable.getColumnModel().getColumn(1);
            column.setPreferredWidth(10);
            ListCellEditor plce2 = new ListCellEditor();
            plce2.setClickCountToStart(0);
            column.setCellEditor(plce2);
            column.setCellRenderer(new com.qbrowser.render.ListCellRenderer());

            pdce3 = new ListCellEditor();
            TableColumn pcolumn3 = pTable.getColumnModel().getColumn(2);
            pdce3.setClickCountToStart(0);
            pdce3.addCellEditorListener(new CellEditorListener() {

                public void editingStopped(ChangeEvent e) {
                    //今表の中にある全部の行をvalidateする

                    //重複チェック用
                    HashSet keycheck = new HashSet();

                    try {
                        for (int hi = 0; hi < property_table.getRowCount(); hi++) {
                            InputProperty hpr = property_table.getPropertyAtRow(hi);

                            if (hpr.getKey() != null) {
                            if (keycheck.contains(hpr.getKey())) {
                                throw new QBrowserPropertyException("Q0019" + MAGIC_SEPARATOR + hpr.getKey() + MAGIC_SEPARATOR + hpr.getProperty_type() +  MAGIC_SEPARATOR + hpr.getProperty_value());
                            } else {
                                //System.out.println("abc");
                                keycheck.add(hpr.getKey());
                            }
                            }

                            hpr.selfValidate();

                        }
                        newmessage1stpanel_user_props_ok = true;
                    } catch (QBrowserPropertyException qpe) {
                        //cmessagefooter.setText(qpe.getMessage());

                        last_user_prop_validate_error = qpe.getMessage();
                        newmessage1stpanel_user_props_ok = false;
                    }

                }

                public void editingCanceled(ChangeEvent e) {}
            });
            pcolumn3.setCellEditor(pdce3);
            pcolumn3.setCellRenderer(new com.qbrowser.render.ListCellRenderer());

            JScrollPane tablePane = new JScrollPane(pTable);
            JPanel pp = new JPanel();
            pp.setLayout(new BorderLayout());

            //プロパティヘッダパネル
            JPanel prop_header_container = new JPanel();
            prop_header_container.setLayout(new BorderLayout());

            JLabel prop_header_label = new JLabel(resources.getString("qkey.msg.msg158"));
            JPanel button_container = new JPanel();
            JButton plus_button = new JButton("+");
            plus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    property_table.add_one_empty_row();
                    property_table.setItemListenerInComboBoxAt((property_table.getRowCount() - 1)
                            , new UserPropertyTypeComboBoxItemListener());
            }

            });

            JButton minus_button = new JButton("-");
            minus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    int sel_row = pTable.getSelectedRow();
                    if (sel_row < 0) sel_row = 0;
                    if (property_table.getRowCount() > 0)
                    property_table.deletePropertyAtRow(sel_row);
            }

            });
            button_container.add(plus_button);
            button_container.add(minus_button);

            prop_header_container.setBorder(BorderFactory.createEtchedBorder());

            prop_header_container.add(prop_header_label, BorderLayout.CENTER);
            prop_header_container.add(button_container, BorderLayout.EAST);

            pp.setBorder(BorderFactory.createEtchedBorder());

            pp.add(prop_header_container, BorderLayout.NORTH);
            pp.add(tablePane, BorderLayout.CENTER);

            newmessageFrame.getContentPane().add(BorderLayout.CENTER, pp);


            southpanel = new JPanel();

            mbodyPanel = new TextMessageInputBodyPanel();
            mbodyPanel.setTitle(resources.getString("qkey.msg.msg159"));
            southpanel.setLayout(new BorderLayout());

            southpanel.add(BorderLayout.CENTER, mbodyPanel);
            currentBodyPanel = mbodyPanel;

            //選択されたラジオボタンにしたがって
            //入力パネルが変更される
            JPanel txtorfilepanel = new JPanel();

            message_type = new JComboBox();
            message_type.setPreferredSize(new Dimension(120, 20));
            message_type.addItem(TEXTMESSAGE);
            message_type.addItem(BYTESMESSAGE);
            message_type.addItem(MAPMESSAGE);
            message_type.addItem(STREAMMESSAGE);
            message_type.addItem(MESSAGE);
            message_type.setSelectedIndex(0);
            message_type.addItemListener(new MessageTypeListener());

            JLabel jl03 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs025"));

            txtorfilepanel.add(jl03);
            txtorfilepanel.add(message_type);

            //Encoding
            penc = new JPanel();
            JLabel jlenc = new JLabel(resources.getString("qkey.msg.msg404"));
            penc.add(jlenc);
            encoding_type = new JComboBox();
            encoding_type.setPreferredSize(new Dimension(100, 20));
            encoding_type.setEditable(true);
            String default_encoding = resources.getString("qkey.msg.msg405");
            encoding_type.addItem(default_encoding);
            //encode_before = default_encoding;
            encoding_type.addItem("UTF8");
            //デフォルト
            //encoding_type.addItem("SJIS");
            encoding_type.addItem("ISO2022JP");
            encoding_type.addItem("EUCJP");
            encoding_type.addItem("UTF-16");
            encoding_type.addItemListener(new MessageEncodingTypeListener());
            penc.add(encoding_type);

            //DeliveryMode
            JPanel pdeliverymode = new JPanel();

            JLabel ldeliverymode = new JLabel(resources.getString("qkey.msg.msg154"));
            cdeliverymode = new JComboBox();
            cdeliverymode.addItem(resources.getString("qkey.msg.msg122"));
            cdeliverymode.addItem(resources.getString("qkey.msg.msg123"));
            cdeliverymode.setPreferredSize(new Dimension(110, 18));
            pdeliverymode.add(ldeliverymode);
            pdeliverymode.add(cdeliverymode);

            //メッセージ送付回数
            JPanel msgkosupanel = new JPanel();
            msgkosupanel.setLayout(new BorderLayout());
            JLabel jl08 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs026"));
            msgkosupanel.add(BorderLayout.CENTER, jl08);

            soufukosu = new JTextField(5);
            soufukosu.addCaretListener(new SoufukosuInputListener());
            soufukosu.setText("1");

            msgkosupanel.add(BorderLayout.EAST, soufukosu);


            messagesentakupanel = new JPanel();
            messagesentakupanel.setLayout(new BorderLayout());
            messagesentakupanel.add(BorderLayout.WEST, txtorfilepanel);
            messagesentakupanel.setBorder(BorderFactory.createEtchedBorder());
            JPanel modecontainer = new JPanel();

            JButton clearbutton = new JButton(resources.getString("qkey.msg.msg216"));
            clearbutton.addActionListener(new NewMessageClearListener());

            modecontainer.add(pdeliverymode);
            modecontainer.add(clearbutton);
            messagesentakupanel.add(BorderLayout.EAST, msgkosupanel);
            messagesentakupanel.add(BorderLayout.CENTER,penc);


            southpanel.add(BorderLayout.NORTH, messagesentakupanel);


            okbutton = new JButton(resourcesamq.getString("qkey.amq.msg.mgs027"));


            matesakiBox1.addItemListener(new AtesakiInputListener());
            okbutton.addActionListener(new NewMessageOKListener());

            JButton cancelbutton = new JButton(resourcesamq.getString("qkey.amq.msg.mgs030"));
            cancelbutton.addActionListener(new NewMessageCancelListener());

            JPanel buttonpanel = new JPanel();
            buttonpanel.setLayout(new BorderLayout());


            JPanel pbuttonpanel = new JPanel();
            pbuttonpanel.add(okbutton);
            pbuttonpanel.add(cancelbutton);

            buttonpanel.add(BorderLayout.EAST, pbuttonpanel);
            cmessagefooter = new JLabel();
            buttonpanel.add(BorderLayout.CENTER, cmessagefooter);

            JPanel bbcontainer = new JPanel();
            bbcontainer.setBorder(BorderFactory.createEtchedBorder());

            buttonpanel.setBorder(BorderFactory.createEtchedBorder());
            bbcontainer.setLayout(new BorderLayout());
            bbcontainer.add(BorderLayout.WEST, modecontainer);
            bbcontainer.add(BorderLayout.SOUTH, buttonpanel);

            southpanel.add(BorderLayout.SOUTH, bbcontainer);



            newmessageFrame.getContentPane().add(BorderLayout.SOUTH, southpanel);
            newmessageFrame.pack();
            if (cleanupmode) {
                mbodyPanel.updateUI();
            }
        }


        //今ブラウズモードで選択されているあて先名を補完する
        ComboBoxEditor editor = qBox.getEditor();
        String orig_name = (String) editor.getItem();
        String name = getPureDestName(orig_name);

            if (orig_name.indexOf(TOPIC_SUFFIX) != -1) {
                mqBox.setSelectedItem(TOPIC_LITERAL);
            } else if (orig_name.indexOf(QUEUE_SUFFIX) != -1) {
                mqBox.setSelectedItem(QUEUE_LITERAL);
            } else {
                mqBox.setSelectedItem(LOCAL_STORE_LITERAL);
            }
        matesakiBox1.setSelectedItem(name);

        newmessageFrame.setLocationRelativeTo(oya);
        newmessageFrame.setVisible(true);
    }

    @Override
    void showNewMessagePanelAsMessageCopy(LocalMessageContainer srcmsg) {
        Message imsg = srcmsg.getMessage();
        try {
            if (imsg == null) {
                imsg = srcmsg.getRealMessage(session);
            }
        } catch (Exception e) {
            popupErrorMessageDialog(e);
        }
        showNewMessagePanelAsMessageCopy((MessageContainer) srcmsg);
        header_table.load(srcmsg);
    }

    @Override
    void showNewMessagePanelAsMessageCopy(MessageContainer srcmsg) {

        if (newmessageFrame != null) {
            newmessageFrame.dispose();
            newmessageFrame = null;
        }

        //int tabindex = tabbedPane.getSelectedIndex();
        //String from_msg_table_with_suffix = tabbedPane.getTitleAt(tabindex);

        cleanupNewMessagePanelObjects();

        Message imsg = srcmsg.getMessage();
        if (imsg == null) {
            try {
              Queue rq = getQueue(getPureDestName(srcmsg.getPureDest_name()));
              imsg = srcmsg.getRealMessageFromBroker(session, rq);
            } catch (Exception reale) {
                reale.printStackTrace();
            }
        }

                // Create popup
                newmessageFrame = new JFrame();
                newmessageFrame.setTitle(resourcesamq.getString("qkey.amq.msg.mgs021"));
                newmessageFrame.setBackground(Color.white);
                newmessageFrame.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg).getImage());
                newmessageFrame.getContentPane().setLayout(new BorderLayout());

            newmessageFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    if (stringEditDialog != null) {
                        stringEditDialog.dispose();
                        stringEditDialog = null;
                    }
                }
            });


            JPanel northpanel = new JPanel();
            northpanel.setLayout(new BorderLayout());

            //宛先入力はコンボボックスに変更
            matesakiBox1 = new JComboBox();

            Dimension dm = matesakiBox1.getPreferredSize();
            dm.setSize(10 * dm.getWidth(), dm.getHeight());
            matesakiBox1.setPreferredSize(dm);
            matesakiBox1.setEditable(false);

            
            //ヘッダパネル
            header_table = new ActiveMQHeaderPropertyTable(0);
            hTable = new JTable(header_table);
            header_table.load(srcmsg);


            hTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            hTable.setDefaultRenderer(Object.class, new StripeTableRendererForProperty());

            //TODO プロパティの多さによって最大200くらいまで動的拡張するようにする
            hTable.setPreferredScrollableViewportSize(new Dimension(500,100));

            DefaultCellEditor hdce = new DefaultCellEditor(getHeaderPropTypeComboBox());
            TableColumn hcolumn = hTable.getColumnModel().getColumn(0);
            hdce.setClickCountToStart(0);
            hcolumn.setCellEditor(hdce);

            hdce2 = new PropTableCellEditor();
            TableColumn hcolumn2 = hTable.getColumnModel().getColumn(1);
            hdce2.setClickCountToStart(0);
            hptc.addItem("Message Group Sequence Number");
            hptc.addItem("Message Group");
            hdce2.addCellEditorListener(new CellEditorListener() {

                public void editingStopped(ChangeEvent e) {
                    //今表の中にある全部の行をvalidateする

                    try {

                         //重複チェック用
                        HashSet keycheck = new HashSet();

                        for (int hi = 0; hi < header_table.getRowCount(); hi++) {
                            Property hpr = header_table.getPropertyAtRow(hi);
                            String key = hpr.getKey();
                            Object val = hpr.getProperty_value();

                            if (key != null) {
                                if (keycheck.contains(key)) {
                                    throw new QBrowserPropertyException("Q0020" + MAGIC_SEPARATOR + key + MAGIC_SEPARATOR + hpr.getProperty_type() + MAGIC_SEPARATOR + val);
                                } else {
                                    keycheck.add(key);
                                }
                            }

                            ActiveMQPropertyUtil.validateJMSHeaderValueType(key, val);

                        }
                        newmessage1stpanelok = true;
                    } catch (QBrowserPropertyException qpe) {
                        //cmessagefooter.setText(qpe.getMessage());

                        last_jmsheader_validate_error = qpe.getMessage();

                        newmessage1stpanelok = false;
                    }

                }

                public void editingCanceled(ChangeEvent e) {}
            });

            hcolumn2.setCellEditor(hdce2);

            JScrollPane htablePane = new JScrollPane(hTable);
            JPanel hp = new JPanel();
            hp.setLayout(new BorderLayout());

            //プロパティヘッダパネル
            JPanel header_header_container = new JPanel();
            header_header_container.setLayout(new BorderLayout());

            JLabel header_header_label = new JLabel(resources.getString("qkey.msg.msg024"));
            JPanel hbutton_container = new JPanel();
            JButton hplus_button = new JButton("+");
            hplus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    header_table.add_one_empty_row();
            }

            });

            JButton hminus_button = new JButton("-");
            hminus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    int sel_row = hTable.getSelectedRow();
                    if (sel_row < 0) sel_row = 0;
                    if (header_table.getRowCount() > 0)
                    header_table.deletePropertyAtRow(sel_row);
            }

            });
            hbutton_container.add(hplus_button);
            hbutton_container.add(hminus_button);

            header_header_container.setBorder(BorderFactory.createEtchedBorder());

            header_header_container.add(header_header_label, BorderLayout.CENTER);
            header_header_container.add(hbutton_container, BorderLayout.EAST);

            hp.setBorder(BorderFactory.createEtchedBorder());

            hp.add(header_header_container, BorderLayout.NORTH);
            hp.add(htablePane, BorderLayout.CENTER);


            northpanel.add(BorderLayout.SOUTH, hp);

            mqBox = new JComboBox();
            mqBox.addItemListener(new SendAtesakiComboBoxItemListener());
            Dimension d = mqBox.getPreferredSize();
            d.setSize(10 * d.getWidth(), d.getHeight());
            mqBox.setPreferredSize(d);
            mqBox.setEditable(false);

            DefaultComboBoxModel model = (DefaultComboBoxModel) mqBox.getModel();
            model.addElement(QUEUE_LITERAL);
            model.addElement(TOPIC_LITERAL);
                        model.addElement(LOCAL_STORE_LITERAL);

            JLabel jl01 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs022"));
            northpanel.add(BorderLayout.WEST, jl01);
            northpanel.add(BorderLayout.EAST, mqBox);

            //宛先名入力エリア
            JPanel atesaki = new JPanel();
            atesaki.setLayout(new BorderLayout());
            JLabel jl02 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs023"));
            atesaki.add(BorderLayout.WEST, jl02);

            //データ入れ込み。デフォルトはQUEUE
            importQueueNamesToMATESAKIBOX1();
            matesakiBox1.setEditable(true);

            atesaki.add(BorderLayout.CENTER, matesakiBox1);
            northpanel.add(BorderLayout.NORTH, atesaki);

            newmessageFrame.getContentPane().add(BorderLayout.NORTH, northpanel);

            //mpropertyPanel = new PropertyPanel();
            //mpropertyPanel.setTitle(resources.getString("qkey.msg.msg158"));
            //newmessageFrame.getContentPane().add(BorderLayout.CENTER, mpropertyPanel);
            property_table = new PropertyInputTable(0);

            //newmessageFrame.getContentPane().add(BorderLayout.CENTER, mpropertyPanel);
            pTable = new JTable(property_table);
            pTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            pTable.setDefaultRenderer(Object.class, new StripeTableRendererForProperty());
            property_table.load(srcmsg);
            for (int pii = 0 ; pii < property_table.getRowCount(); pii++) {
                property_table.setItemListenerInComboBoxAt(pii, new UserPropertyTypeComboBoxItemListener());
                property_table.setMouseListenerInTextAreaAt(pii, new UserPropertyStringPropertyMouseListener());
            }
            pTable.setRowHeight(20);

            //TODO プロパティの多さによって最大200くらいまで動的拡張するようにする
            pTable.setPreferredScrollableViewportSize(new Dimension(500,120));

            TableColumn column0 = pTable.getColumnModel().getColumn(0);
            pdce1 = new DefaultCellEditor(new JTextField());
            pdce1.setClickCountToStart(0);
            column0.setCellEditor(pdce1);

            TableColumn column = pTable.getColumnModel().getColumn(1);

            column.setPreferredWidth(10);
            ListCellEditor plce2 = new ListCellEditor();
            plce2.setClickCountToStart(0);
            column.setCellEditor(plce2);
            column.setCellRenderer(new com.qbrowser.render.ListCellRenderer());

            pdce3 = new ListCellEditor();
            TableColumn pcolumn3 = pTable.getColumnModel().getColumn(2);
            pdce3.setClickCountToStart(0);
            pdce3.addCellEditorListener(new CellEditorListener() {

                public void editingStopped(ChangeEvent e) {
                    //今表の中にある全部の行をvalidateする

                    //重複チェック用
                    HashSet keycheck = new HashSet();

                    try {
                        for (int hi = 0; hi < property_table.getRowCount(); hi++) {
                            InputProperty hpr = property_table.getPropertyAtRow(hi);

                            if (hpr.getKey() != null) {
                            if (keycheck.contains(hpr.getKey())) {
                                throw new QBrowserPropertyException("Q0019" + MAGIC_SEPARATOR + hpr.getKey() + MAGIC_SEPARATOR + hpr.getProperty_type() +  MAGIC_SEPARATOR + hpr.getProperty_value());
                            } else {
                                //System.out.println("abc");
                                keycheck.add(hpr.getKey());
                            }
                            }

                            hpr.selfValidate();

                        }
                        newmessage1stpanel_user_props_ok = true;
                    } catch (QBrowserPropertyException qpe) {
                        //cmessagefooter.setText(qpe.getMessage());

                        last_user_prop_validate_error = qpe.getMessage();
                        newmessage1stpanel_user_props_ok = false;
                    }

                }

                public void editingCanceled(ChangeEvent e) {}
            });
            pcolumn3.setCellEditor(pdce3);
            pcolumn3.setCellRenderer(new com.qbrowser.render.ListCellRenderer());

            JScrollPane tablePane = new JScrollPane(pTable);
            JPanel pp = new JPanel();
            pp.setLayout(new BorderLayout());

            //プロパティヘッダパネル
            JPanel prop_header_container = new JPanel();
            prop_header_container.setLayout(new BorderLayout());

            JLabel prop_header_label = new JLabel(resources.getString("qkey.msg.msg158"));
            JPanel button_container = new JPanel();
            JButton plus_button = new JButton("+");
            plus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    property_table.add_one_empty_row();
                    property_table.setItemListenerInComboBoxAt((property_table.getRowCount() - 1)
                            , new UserPropertyTypeComboBoxItemListener());
            }

            });

            JButton minus_button = new JButton("-");
            minus_button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                    int sel_row = pTable.getSelectedRow();
                    if (sel_row < 0) sel_row = 0;
                    if (property_table.getRowCount() > 0)
                    property_table.deletePropertyAtRow(sel_row);
            }

            });
            button_container.add(plus_button);
            button_container.add(minus_button);

            prop_header_container.setBorder(BorderFactory.createEtchedBorder());

            prop_header_container.add(prop_header_label, BorderLayout.CENTER);
            prop_header_container.add(button_container, BorderLayout.EAST);

            pp.setBorder(BorderFactory.createEtchedBorder());

            pp.add(prop_header_container, BorderLayout.NORTH);
            pp.add(tablePane, BorderLayout.CENTER);

            newmessageFrame.getContentPane().add(BorderLayout.CENTER, pp);


            southpanel = new JPanel();

            mbodyPanel = new TextMessageInputBodyPanel();
            mbodyPanel.setTitle(resources.getString("qkey.msg.msg159"));
            southpanel.setLayout(new BorderLayout());

            southpanel.add(BorderLayout.CENTER, mbodyPanel);
            currentBodyPanel = mbodyPanel;

            //選択されたラジオボタンにしたがって
            //入力パネルが変更される
            JPanel txtorfilepanel = new JPanel();

            message_type = new JComboBox();
            message_type.setPreferredSize(new Dimension(120, 20));
            message_type.addItem(TEXTMESSAGE);
            message_type.addItem(BYTESMESSAGE);
            message_type.addItem(MAPMESSAGE);
            message_type.addItem(STREAMMESSAGE);
            message_type.addItem(MESSAGE);
            message_type.setSelectedIndex(0);
            message_type.addItemListener(new MessageTypeListener());

            JLabel jl03 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs025"));

            txtorfilepanel.add(jl03);
            txtorfilepanel.add(message_type);

            //Encoding
            penc = new JPanel();
            JLabel jlenc = new JLabel(resources.getString("qkey.msg.msg404"));
            penc.add(jlenc);
            encoding_type = new JComboBox();
            encoding_type.setPreferredSize(new Dimension(100, 20));
            encoding_type.setEditable(true);
            String default_encoding = resources.getString("qkey.msg.msg405");
            encoding_type.addItem(default_encoding);
            //encode_before = default_encoding;
            encoding_type.addItem("UTF8");
            //デフォルト
            //encoding_type.addItem("SJIS");
            encoding_type.addItem("ISO2022JP");
            encoding_type.addItem("EUCJP");
            encoding_type.addItem("UTF-16");
            encoding_type.addItemListener(new MessageEncodingTypeListener());
            penc.add(encoding_type);

            //DeliveryMode
            JPanel pdeliverymode = new JPanel();

            JLabel ldeliverymode = new JLabel(resources.getString("qkey.msg.msg154"));
            cdeliverymode = new JComboBox();
            cdeliverymode.addItem(resources.getString("qkey.msg.msg122"));
            cdeliverymode.addItem(resources.getString("qkey.msg.msg123"));
            cdeliverymode.setPreferredSize(new Dimension(110, 18));
            pdeliverymode.add(ldeliverymode);
            pdeliverymode.add(cdeliverymode);

            //メッセージ送付回数
            JPanel msgkosupanel = new JPanel();
            msgkosupanel.setLayout(new BorderLayout());
            JLabel jl08 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs026"));
            msgkosupanel.add(BorderLayout.CENTER, jl08);

            soufukosu = new JTextField(5);
            soufukosu.addCaretListener(new SoufukosuInputListener());
            soufukosu.setText("1");

            msgkosupanel.add(BorderLayout.EAST, soufukosu);


            messagesentakupanel = new JPanel();
            messagesentakupanel.setLayout(new BorderLayout());
            messagesentakupanel.add(BorderLayout.WEST, txtorfilepanel);
            messagesentakupanel.setBorder(BorderFactory.createEtchedBorder());
            JPanel modecontainer = new JPanel();

            JButton clearbutton = new JButton(resources.getString("qkey.msg.msg216"));
            clearbutton.addActionListener(new NewMessageClearListener());

            modecontainer.add(pdeliverymode);
            modecontainer.add(clearbutton);
            messagesentakupanel.add(BorderLayout.EAST, msgkosupanel);
            messagesentakupanel.add(BorderLayout.CENTER,penc);


            southpanel.add(BorderLayout.NORTH, messagesentakupanel);


            okbutton = new JButton(resourcesamq.getString("qkey.amq.msg.mgs027"));

            matesakiBox1.addItemListener(new AtesakiInputListener());
            okbutton.addActionListener(new NewMessageOKListener());

            JButton cancelbutton = new JButton(resourcesamq.getString("qkey.amq.msg.mgs028"));
            cancelbutton.addActionListener(new NewMessageCancelListener());

            JPanel buttonpanel = new JPanel();
            buttonpanel.setLayout(new BorderLayout());


            JPanel pbuttonpanel = new JPanel();
            pbuttonpanel.add(okbutton);
            pbuttonpanel.add(cancelbutton);

            buttonpanel.add(BorderLayout.EAST, pbuttonpanel);
            cmessagefooter = new JLabel();
            buttonpanel.add(BorderLayout.CENTER, cmessagefooter);

            JPanel bbcontainer = new JPanel();
            bbcontainer.setBorder(BorderFactory.createEtchedBorder());

            buttonpanel.setBorder(BorderFactory.createEtchedBorder());
            bbcontainer.setLayout(new BorderLayout());
            bbcontainer.add(BorderLayout.WEST, modecontainer);
            bbcontainer.add(BorderLayout.SOUTH, buttonpanel);

            southpanel.add(BorderLayout.SOUTH, bbcontainer);
            newmessageFrame.getContentPane().add(BorderLayout.SOUTH, southpanel);
            newmessageFrame.pack();

            //メッセージ種別ごとにBodyデータをローディングする
            if (imsg instanceof TextMessage) {
                message_type.setSelectedItem(TEXTMESSAGE);
                southpanel.remove(currentBodyPanel);
                try {
                mbodyPanel.textArea.setText(((TextMessage)imsg).getText());
                } catch (JMSException jmsinputex) {}
                southpanel.add(BorderLayout.CENTER, mbodyPanel);

                currentBodyPanel = mbodyPanel;
                southpanel.updateUI();
            } else if (imsg instanceof BytesMessage) {
                message_type.setSelectedItem(BYTESMESSAGE);
                southpanel.remove(currentBodyPanel);
                createBytesMessageBodyPanel();
                mfilepath.setText(resources.getString("qkey.msg.msg219"));
                passthrough_bytesmessage = (BytesMessage)imsg;
                southpanel.add(BorderLayout.CENTER, mfilebodyPanel);
                currentBodyPanel = mfilebodyPanel;
                southpanel.updateUI();

            } else if (imsg instanceof MapMessage) {
                message_type.setSelectedItem(MAPMESSAGE);
                southpanel.remove(currentBodyPanel);
                createMapMessageBodyPanel((MapMessage)imsg);
                southpanel.add(BorderLayout.CENTER, mapmBodyPanel);
                currentBodyPanel = mapmBodyPanel;
                southpanel.updateUI();
            } else if (imsg instanceof StreamMessage) {
                message_type.setSelectedItem(STREAMMESSAGE);
                southpanel.remove(currentBodyPanel);
                createStreamMessageBodyPanel((StreamMessage)imsg);
                southpanel.add(BorderLayout.CENTER, smBodyPanel);
                currentBodyPanel = smBodyPanel;
                southpanel.updateUI();
            } else if (imsg instanceof Message) {
                //Message
                message_type.setSelectedItem(MESSAGE);
                southpanel.remove(currentBodyPanel);
                JPanel plain_panel = new JPanel();
                JLabel message_label = new JLabel();
                message_label.setText(resources.getString("qkey.msg.msg244"));
                plain_panel.add(BorderLayout.CENTER ,message_label);
                southpanel.add(plain_panel);
                currentBodyPanel = plain_panel;
                southpanel.updateUI();
            }



            newmessageFrame.getContentPane().add(BorderLayout.SOUTH, southpanel);
            newmessageFrame.pack();




        //今ブラウズモードで選択されているあて先名を補完する
        ComboBoxEditor editor = qBox.getEditor();
        String orig_name = (String) editor.getItem();
        String name = getPureDestName(orig_name);

            if (orig_name.indexOf(TOPIC_SUFFIX) != -1) {
                mqBox.setSelectedItem(TOPIC_LITERAL);
            } else if (orig_name.indexOf(QUEUE_LITERAL) != -1){
                mqBox.setSelectedItem(QUEUE_LITERAL);
            } else if (orig_name.indexOf(LOCAL_STORE_LITERAL) != -1) {
                mqBox.setSelectedItem(LOCAL_STORE_LITERAL);
            }
        matesakiBox1.setSelectedItem(name);

        newmessageFrame.setLocationRelativeTo(oya);
        newmessageFrame.setVisible(true);
    }

    @Override
    public void showForwardWindow(int x, int y, boolean deleteSrcMessageAfterForward) {

        if (forwardDialog != null) {
            forwardDialog.dispose();
            forwardDialog = null;
        }

        // Create popup
        if (forwardDialog == null) {
            forwardDialog = new JDialog();

            if (deleteSrcMessageAfterForward) {
              forwardDialog.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg).getImage());
            } else {
              forwardDialog.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Move).getImage());
            }


            forwardDialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    //
                }
            });

            forwardDialog.getContentPane().setLayout(new BorderLayout());

            forwardmsgPanel = new JPanel();
            forwardmsgPanel.setLayout(new BorderLayout());

            JPanel forwardmsg = new JPanel();
            forwardDialog.setSize(200, 200);

            JLabel forwardlabel = null;
            if (deleteSrcMessageAfterForward) {
                forwardDialog.setTitle(resources.getString("qkey.msg.msg224"));
                forwardlabel = new JLabel(resources.getString("qkey.msg.msg225"));
            } else {
                forwardDialog.setTitle(resources.getString("qkey.msg.msg133"));
                forwardlabel = new JLabel(resources.getString("qkey.msg.msg134"));
            }

       forwardtextfield = new JTextField(36);
        matesakiBox2 = new JComboBox();
        Dimension dm = matesakiBox2.getPreferredSize();
        dm.setSize(10 * dm.getWidth(), dm.getHeight());
        matesakiBox2.setPreferredSize(dm);

        importTopicNamesToMATESAKIBOX2();
        matesakiBox2.setEditable(true);

        JPanel expl = new JPanel();
        expl.setLayout(new BorderLayout());

        forwardBox = new JComboBox();
        forwardBox.addItemListener(new SendForwardAtesakiComboBoxItemListener());
        Dimension d = forwardBox.getPreferredSize();
        d.setSize(110 , d.getHeight());
        forwardBox.setPreferredSize(d);
        forwardBox.setEditable(false);

        DefaultComboBoxModel model = (DefaultComboBoxModel) forwardBox.getModel();
        model.addElement(QUEUE_LITERAL);
        model.addElement(TOPIC_LITERAL);
        model.addElement(LOCAL_STORE_LITERAL);

        JPanel tqboxpanel = new JPanel();
        tqboxpanel.setLayout(new BorderLayout());
        JLabel txboxlabel = new JLabel(resources.getString("qkey.msg.msg044"));
        tqboxpanel.add(BorderLayout.WEST,txboxlabel);
        JPanel dp = new JPanel(); dp.setLayout(new BorderLayout());
        dp.add(BorderLayout.WEST, forwardBox);
        tqboxpanel.add(BorderLayout.CENTER, dp);
        expl.add(BorderLayout.NORTH, tqboxpanel);
        expl.add(BorderLayout.CENTER, forwardlabel);

        forwardmsg.add(matesakiBox2);
        forwardmsgPanel.add(BorderLayout.NORTH, expl);
        forwardmsgPanel.add(BorderLayout.CENTER, forwardmsg);
        JButton okbutton1 = new JButton("               OK               ");
        okbutton1.addActionListener(new ForwardOKListener(deleteSrcMessageAfterForward));
        JButton cancelbutton = new JButton("         " + resources.getString("qkey.msg.msg129") + "             ");
        cancelbutton.addActionListener(new ForwardCancelListener());

        JPanel pbuttonpanel = new JPanel();
        pbuttonpanel.setLayout(new BorderLayout());
        pbuttonpanel.add(BorderLayout.WEST, okbutton1);
        pbuttonpanel.add(BorderLayout.CENTER, cancelbutton);

        forwardmsgPanel.add(BorderLayout.SOUTH, pbuttonpanel);


         forwardDialog.getContentPane().add(BorderLayout.NORTH, forwardmsgPanel);
         forwardDialog.setLocationRelativeTo(oya);

         forwardDialog.pack();

        }

        forwardDialog.setVisible(true);

    }


    @Override
    public void showSearchWindow() {

        // Create popup
        if (searchDialog == null) {
         searchDialog = new JDialog();
         searchDialog.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.AtesakiDetails).getImage());

         searchDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });

         searchDialog.setLocation(380,95);
         searchDialog.getContentPane().setLayout(new BorderLayout());

         searchmsgPanel = new JPanel();
         searchmsgPanel.setLayout(new BorderLayout());

         JPanel searchmsg = new JPanel();
         searchDialog.setSize(200, 200);
         searchDialog.setTitle(resourcesamq.getString("qkey.amq.msg.mgs031"));

         searchtextfield = new JTextField(36);

         JLabel searchlabel = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs032"));
         JLabel searchlabel2 = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs033"));
         JPanel expl = new JPanel();
         expl.setLayout(new BorderLayout());
         tqBox = new JComboBox();
         tqBox.setEditable(true);

         JPanel tqboxpanel = new JPanel();
         tqboxpanel.setLayout(new BorderLayout());
         JLabel txboxlabel = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs034"));
         tqboxpanel.add(BorderLayout.WEST,txboxlabel);
         tqboxpanel.add(BorderLayout.CENTER,tqBox);
         expl.add(BorderLayout.NORTH, tqboxpanel);
         expl.add(BorderLayout.CENTER, searchlabel);
         expl.add(BorderLayout.SOUTH, searchlabel2);

         String selectedDest = (String)tqBox.getSelectedItem();

         //テンプレート
         searchTemplateBox = new JComboBox();
         searchTemplateBox.addItemListener(new SearchTemplateItemListener());
         ArrayList extracted_history = QBrowserUtil.getHistoryFromFile("search_history_amq");
         QBrowserUtil.ArrayListToJComboBox(extracted_history, searchTemplateBox);

         if (extracted_history.size() == 0) {
         DefaultComboBoxModel model = (DefaultComboBoxModel) searchTemplateBox.getModel();
         model.addElement("abc = 'abc' AND efg = 'efg' ");
         model.addElement("JMSPriority > 4 ");
         model.addElement("JMSMessageID = 'ID:P<114325.1240914634312.2>'");
         model.addElement("JMSDeliveryMode = 'PERSISTENT'");
         model.addElement("JMSDeliveryMode = 'NON_PERSISTENT'");
         model.addElement("Country IN ('UK', 'US', 'France') ");
         model.addElement("Country NOT= 'UK'");
         model.addElement("phone LIKE '12%3' ");
         model.addElement("word LIKE 'l_se' ");
         model.addElement("prop_name IS NULL");
         model.addElement("prop_name IS NOT NULL");
         model.addElement("JMSTimestamp = 1240042958265");
         }

         searchmsg.add(searchtextfield);
         searchmsgPanel.add(BorderLayout.NORTH, expl);
         searchmsgPanel.add(BorderLayout.CENTER, searchmsg);
         JButton okbutton1 = new JButton("               OK               ");
         okbutton1.addActionListener(new SearchOKListener(this));
         JButton cancelbutton = new JButton("         " + resourcesamq.getString("qkey.amq.msg.mgs030") + "             ");
         cancelbutton.addActionListener(new SearchCancelListener());

         JPanel pbuttonpanel = new JPanel();
         pbuttonpanel.setLayout(new BorderLayout());
         pbuttonpanel.add(BorderLayout.WEST, okbutton1);
         pbuttonpanel.add(BorderLayout.CENTER, cancelbutton);

         JPanel temppanel = new JPanel();
         temppanel.setLayout(new BorderLayout());
         JLabel templabel = new JLabel(resourcesamq.getString("qkey.amq.msg.mgs036"));

         temppanel.add(BorderLayout.NORTH, templabel);
         temppanel.add(BorderLayout.CENTER, searchTemplateBox);
         temppanel.add(BorderLayout.SOUTH, pbuttonpanel);

         searchmsgPanel.add(BorderLayout.SOUTH, temppanel);


         searchDialog.getContentPane().add(BorderLayout.NORTH, searchmsgPanel);
         searchDialog.pack();

        }

         copyComboBox();
         searchDialog.setLocationRelativeTo(oya);
         searchDialog.setVisible(true);
    }

 
    @Override
    public void showSubscribeWindow(String selectedTopicName) {

         if (subscribeDialog != null) {
            subscribeDialog.dispose();
            subscribeDialog = null;
         }

         subscribeDialog = new JDialog();
         subscribeDialog.setIconImage(QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Subscribe).getImage());


         subscribeDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });

         subscribeDialog.setLocation(380,95);
         subscribeDialog.getContentPane().setLayout(new BorderLayout());

         subscribemsgPanel = new JPanel();
         subscribemsgPanel.setLayout(new BorderLayout());

         JPanel subscribemsg = new JPanel();
         subscribeDialog.setSize(200, 200);
         subscribeDialog.setTitle(resources.getString("qkey.msg.msg131"));

         subscribetextfield = new JTextField(36);
         matesakiBox3 = new JComboBox();
         matesakiBox3.setPreferredSize((new Dimension(250, 20)));

         importTopicNamesToMATESAKIBOX3();
         matesakiBox3.setEditable(true);

         JLabel subscribelabel = new JLabel(resources.getString("qkey.msg.msg126"));
         JPanel expl = new JPanel();
         expl.setLayout(new BorderLayout());

         JPanel tqboxpanel = new JPanel();
         tqboxpanel.setLayout(new BorderLayout());
        expl.add(BorderLayout.NORTH, tqboxpanel);
        expl.add(BorderLayout.CENTER, subscribelabel);

         //テンプレート
         subscribeTemplateBox = new JComboBox();
         subscribeTemplateBox.addItemListener(new SubscribeTemplateItemListener());
         DefaultComboBoxModel model = (DefaultComboBoxModel) subscribeTemplateBox.getModel();

         subscribeTemplateBox.setPreferredSize(new Dimension(250, 20));

         subscribemsg.add(matesakiBox3);

         subscribemsgPanel.add(BorderLayout.NORTH, expl);
         subscribemsgPanel.add(BorderLayout.CENTER, subscribemsg);
         JButton okbutton1 = new JButton("               OK               ");
         okbutton1.addActionListener(new SubscribeOKListener());
         JButton cancelbutton = new JButton("         " + resources.getString("qkey.msg.msg129") + "             ");
         cancelbutton.addActionListener(new SubscribeCancelListener());

         JPanel pbuttonpanel = new JPanel();
         pbuttonpanel.setLayout(new BorderLayout());
         pbuttonpanel.add(BorderLayout.WEST, okbutton1);
         pbuttonpanel.add(BorderLayout.CENTER, cancelbutton);

         JPanel temppanel = new JPanel();
         temppanel.setLayout(new BorderLayout());
         JLabel templabel = new JLabel(resources.getString("qkey.msg.msg130"));

         if (selectedTopicName != null) {
             matesakiBox3.setSelectedItem(selectedTopicName);
             matesakiBox3.setEnabled(false);
             subscribeTemplateBox.setEnabled(false);
         }

         temppanel.add(BorderLayout.NORTH, templabel);
         JPanel tdpanel = new JPanel();
         tdpanel.add(subscribeTemplateBox);
         temppanel.add(BorderLayout.CENTER, tdpanel);


         JPanel centerPanel = new JPanel();
         centerPanel.setLayout(new BorderLayout());
         centerPanel.add(BorderLayout.NORTH, temppanel);

         localstoreBox = new JComboBox();
         localstoreBox.setPreferredSize(new Dimension(250, 20));
         importLocalStoreNamesToLOCALSTOREBOX();

         JPanel temppanel2 = new JPanel();
         temppanel2.setLayout(new BorderLayout());
         JLabel templabel2 = new JLabel(resources.getString("qkey.msg.msg274"));
         temppanel2.add(BorderLayout.NORTH, templabel2);
         JPanel dpanel = new JPanel();
         dpanel.add(localstoreBox);
         temppanel2.add(BorderLayout.CENTER, dpanel);
         temppanel2.add(BorderLayout.SOUTH, pbuttonpanel);
         centerPanel.add(BorderLayout.CENTER, temppanel2);


         subscribemsgPanel.add(BorderLayout.SOUTH, centerPanel);

         subscribeDialog.getContentPane().add(BorderLayout.NORTH, subscribemsgPanel);
         subscribeDialog.pack();

        
         subscribeDialog.setLocationRelativeTo(oya);
         subscribeDialog.setVisible(true);
    }

    @Override
    public javax.jms.Destination convertLocalDestinationToVendorDestination(javax.jms.Destination dest) throws Exception {


        if (dest == null) {
            return null;
        }

        if (dest instanceof LocalDestination) {

            if (dest instanceof LocalTopic) {
                String destname = ((Topic) dest).getTopicName();
                return session.createTopic(destname);
            } else {
                String destname = ((Queue) dest).getQueueName();
                return session.createQueue(destname);
            }

        } else {
            return dest;
        }
    }


    @Override
    public javax.jms.Destination convertVendorDestinationToLocalDestination(javax.jms.Destination dest) throws Exception {

        if (dest == null) {
            return null;
        }

        if (dest instanceof LocalDestination) {
            return dest;
        } else {

            if (dest instanceof Topic) {
                String destname = ((Topic) dest).getTopicName();
                LocalTopic lt = new LocalTopic(destname);
                lt.setOriginalDestinationWithSuffix(destname + TOPIC_SUFFIX);
                return lt;
            } else {
                String destname = ((Queue) dest).getQueueName();
                LocalQueue lq = new LocalQueue(destname);
                lq.setOriginalDestinationWithSuffix(destname + QUEUE_SUFFIX);
                return lq;
            }

        }
    }


    @Override
    javax.jms.Queue getQueue(String purename) throws Exception {
           return session.createQueue(purename);
    }

    public void searchQueues() throws Exception {
          Collection col = bf.getQueues();
          
          Iterator icol = col.iterator();
          while (icol.hasNext()) {
              Object obj = icol.next();
              QueueViewMBean qvm = (QueueViewMBean)obj;
              String queue_name = qvm.getName();
              finalDestinationNames.add(queue_name);
              destinationNamesForDisplayQueue.add(queue_name);
              destinationViewObjectCacheForQueue.put(queue_name, qvm);

          }
    }

    public void searchTopics() throws Exception {
          Collection col = bf.getTopics();
          Iterator icol = col.iterator();
          while (icol.hasNext()) {
              Object obj = icol.next();
              TopicViewMBean tvm = (TopicViewMBean)obj;
              String topic_name = tvm.getName();
              destinationNamesForDisplayTopic.add(topic_name);
              destinationViewObjectCacheForTopic.put(topic_name, tvm);

          }
    }

    @Override
    void showDetails(MessageContainer msg, int msgno) {
        Message imsg = msg.getMessage();
        if (imsg == null) {
            try {
                Queue queue = getQueue(msg.getPureDest_name());
                imsg = msg.getRealMessageFromBroker(session, queue);
                //mcにあらためて追加ヘッダを入れる
                //追加ヘッダ属性セット
                ArrayList additional = new ArrayList();
                org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message) imsg;

                String jgroupid = acm.getGroupID();
                //if ((jgroupid != null) && (jgroupid.length() != 0)) {
                    Property jgroupidp = new Property();
                    jgroupidp.setKey("Message Group");
                    jgroupidp.setProperty_type(Property.STRING_TYPE);
                    jgroupidp.setProperty_value(jgroupid);
                    additional.add(jgroupidp);
                //}

                int jgsq = acm.getGroupSequence();

                //if (jgsq != 0) {
                    Property jgsqp = new Property();
                    jgsqp.setKey("Message Group Sequence Number");
                    jgsqp.setProperty_type(Property.INT_TYPE);
                    jgsqp.setProperty_value(jgsq);
                    additional.add(jgsqp);
                //}

                msg.setAdditionalHeaders(additional);
            } catch (Exception reale) {
                reale.printStackTrace();
            }
        }

        super.showDetails(msg, msgno);
        amq_header_table = new ActiveMQHeaderPropertyTable(0);
        //System.out.println("msg = " + msg);
        int retv = amq_header_table.loadAllProperties(msg);
        //details_headertable.loadAllProperties(msg);
        //ReadOnlyActiveMQHeaderPropertyTable roam = new ReadOnlyActiveMQHeaderPropertyTable(0);
        //hhTable.setModel(roam);
        //int rt = roam.loadAllProperties(msg);
        activemqbrowsercache.setCurrentActiveMQHeaderTable(msg.getVmsgid(), amq_header_table);
        JTable hhTable = new JTable(amq_header_table);

        TableColumn hcolumn = hhTable.getColumnModel().getColumn(0);
        hcolumn.setPreferredWidth(220);

        hcolumn = hhTable.getColumnModel().getColumn(1);
        hcolumn.setPreferredWidth(280);

        hhTable.setPreferredScrollableViewportSize(new Dimension(500,192));
        hhTable.updateUI();

        //detailsFrame.setSize(new Dimension(500, 720));
        //detailsFrame.getRootPane().updateUI();
    }

    @Override
    void showDetails(LocalMessageContainer msg, int msgno) {
        try {
            Message imsg = msg.getMessage();
            if (imsg == null) {
                imsg = msg.getRealMessage(session);
            }

            showDetails((MessageContainer) msg, msgno);
            ActiveMQHeaderPropertyTable details_headertable =
                    activemqbrowsercache.getCurrentActiveMQHeaderTable(msg.getVmsgid());
            details_headertable.loadAllProperties(msg);
        } catch (Exception e) {
            popupErrorMessageDialog(e);
        }
    }

    void setDefaultHeaderValue(Message message) {
        if (message != null) {
            try {
            message.setJMSPriority(message.DEFAULT_PRIORITY);
            message.setJMSExpiration(message.DEFAULT_TIME_TO_LIVE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    MessageContainer copyMessageContainer(MessageContainer srcmsg) throws Exception {
        if (srcmsg == null) return null;

        MessageContainer newmc = new MessageContainer();
        Message frommsg = srcmsg.getMessage();
        Message tomsg = null;
        if (frommsg != null) {
            tomsg = copyMessage(frommsg);
        }
        newmc.setMessage(tomsg);
        newmc.setDest_name_with_suffix(srcmsg.getDest_name_with_suffix());
        newmc.setVmsgid(srcmsg.getVmsgid());
        newmc.setVcorrelationid(srcmsg.getVcorrelationid());
        newmc.setVdeliverymode(srcmsg.getVdeliverymode());
        newmc.setVexpiration(srcmsg.getVexpiration());
        newmc.setVjms_type(srcmsg.getVjms_type());
        newmc.setVpriority(srcmsg.getVpriority());
        newmc.setVredelivered(srcmsg.isVredelivered());
        newmc.setVdest(srcmsg.getVdest());
        newmc.setVreplyto(srcmsg.getVreplyto());


        if (frommsg != null) {
        ArrayList additional = new ArrayList();
        org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)frommsg;

            String jgroupid = acm.getGroupID();
            if ((jgroupid != null) && (jgroupid.length() != 0)) {
                Property jgroupidp = new Property();
                jgroupidp.setKey("Message Group");
                jgroupidp.setProperty_type(Property.STRING_TYPE);
                jgroupidp.setProperty_value(jgroupid);
                additional.add(jgroupidp);
            }

            int jgsq = acm.getGroupSequence();

            if (jgsq != 0) {
                Property jgsqp = new Property();
                jgsqp.setKey("Message Group Sequence Number");
                jgsqp.setProperty_type(Property.INT_TYPE);
                jgsqp.setProperty_value(jgsq);
                additional.add(jgsqp);
            }

            newmc.setAdditionalHeaders(additional);

        } else {
            newmc.setAdditionalHeaders(srcmsg.getAdditionalHeaders());
        }

        return newmc;

    }

    MessageContainer copyMessageContainerForFromLSMessage(MessageContainer srcmsg) throws Exception {
        if (srcmsg == null) return null;

        MessageContainer newmc = new MessageContainer();
        Message frommsg = srcmsg.getMessage();
        Message tomsg = null;
        if (frommsg != null) {
            tomsg = copyMessage(frommsg);
        }

        //ローカルファイル起源で、リプレイされたメッセージには
        //そもそもActiveMQ用のヘッダがすっぽり抜けている。
        //しかし、ラッパのLocalMessageContainerがadditional_headersとして
        //きちんと保持しているのでそちらからメッセージにセットする

        newmc.setMessage(tomsg);
        newmc.setDest_name_with_suffix(srcmsg.getDest_name_with_suffix());
        newmc.setVmsgid(srcmsg.getVmsgid());
        newmc.setVcorrelationid(srcmsg.getVcorrelationid());
        newmc.setVdeliverymode(srcmsg.getVdeliverymode());
        newmc.setVexpiration(srcmsg.getVexpiration());
        newmc.setVjms_type(srcmsg.getVjms_type());
        newmc.setVpriority(srcmsg.getVpriority());
        newmc.setVredelivered(srcmsg.isVredelivered());
        newmc.setVdest(srcmsg.getVdest());
        newmc.setVreplyto(srcmsg.getVreplyto());

        org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)tomsg;

        ArrayList additionalhv = srcmsg.getAdditionalHeaders();
        for (int i = 0; i < additionalhv.size(); i++) {
            Property pv = (Property)additionalhv.get(i);
            if (pv.getKey().equals("Message Group")) {
                acm.setGroupID(pv.getProperty_valueASString());
            } else if (pv.getKey().equals("Message Group Sequence Number")) {
                acm.setGroupSequence(pv.getProperty_valueASInt());
            }

        }

        newmc.setAdditionalHeaders(srcmsg.getAdditionalHeaders());

        return newmc;

    }



    @Override
    void connect() throws JMSException {

            try {
              bf = new RemoteJMXBrokerFacade();

              //bf.setJmxRole(jmxRole);
              //bf.setJmxPassword(jmxPassword);
              //bf.setJmxUrl(jmxUrl);
              //bf.getBrokerAdmin();
              System.setProperty("webconsole.jmx.url", jmxUrl);
              System.setProperty("webconsole.jmx.user",jmxRole);
              System.setProperty("webconsole.jmx.password",jmxPassword);

              SystemPropertiesConfiguration configuration = new SystemPropertiesConfiguration();
              bf.setConfiguration(configuration);
              bf.getBrokerAdmin();
              
            } catch (Exception ne) {
                throw new JMSException(resourcesamq.getString("qkey.amq.msg.mgs007"));
            }

            setFooter(resourcesamq.getString("qkey.amq.msg.mgs006") + " " +
                    jmxUrl + "...");
            initJMS();

            try {
                initDestListConsumer();
            } catch (JMSException e) {
                System.err.println(e.getMessage());
                //e.printStackTrace();
            }
            connection.start();
            setFooter(resourcesamq.getString("qkey.amq.msg.mgs008")+ " " + jmxUrl);

    }

    @Override
    void forwardMessage(ArrayList messages,String from_msg_table_with_suffix , String forward_target_name, String forward_target_type, boolean deleteSrcMessageAfterForward, boolean showMessageDialog) throws Exception {
        //メッセージを転送する

        //FROMのパネルを判別する
        //int tabindex = tabbedPane.getSelectedIndex();
        //String from_msg_table_with_suffix = tabbedPane.getTitleAt(tabindex);
        JTable fromTable = (JTable) jtableins.get(from_msg_table_with_suffix);



        //宛先別に送りを作成
        if (forward_target_type.equals(QUEUE_LITERAL)) {
            //Queue
            String cjp = getPureDestName(forward_target_name);
            Queue queue = session.createQueue(cjp);
            MessageProducer sender = session.createProducer(queue);

            TextArea ta = new TextArea("", 10, 90, TextArea.SCROLLBARS_BOTH);
            ta.setEditable(true);
            int size = messages.size();
            int appropriaterowsize = size + 2;
                if (appropriaterowsize > 15) {
                        appropriaterowsize = 14;
                }

             ta.setRows(appropriaterowsize);


            //Queue送信進捗情報

            if (showMessageDialog) {

                    String dispid = "ForwardMessageProgresDialog";
                    DisplayMsgDialogRunner dpr = new DisplayMsgDialogRunner(dispid ,resources.getString("qkey.msg.msg053"), ta,
                                  QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg), oya_frame);
                    Thread dprth = new Thread(dpr);
                    //display_threads.add(dprth);
                    DisplayDialogThreadPool.addDisplayThread(dpr, dprth);
                    dprth.start();

                    while (!dpr.isStarted()) {
                     try {
                       Thread.sleep(100);
                     } catch (Throwable thex) {}
                     }
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
            String datef = df.format(new Date(System.currentTimeMillis()));
            ta.append(resources.getString("qkey.msg.msg054") + forward_target_name + resources.getString("qkey.msg.msg055") + "\n");
            ta.append(resources.getString("qkey.msg.msg056") + datef + "\n");

            StringBuilder mediumbuffer = new StringBuilder();
            String msg057 = resources.getString("qkey.msg.msg057");
            String msg058 = resources.getString("qkey.msg.msg058");
            String msg059 = resources.getString("qkey.msg.msg059");
            String msg060 = resources.getString("qkey.msg.msg060");

            ArrayList<Integer> del_targets = new ArrayList();
            ArrayList<Integer> del_tpc_targets = new ArrayList();

            for (int i = 0; i < size; i++) {
                //Fromが本物のメッセージか、ローカルでリプレイされたかによって挙動を変える

                Object mobj = messages.get(i);
                Message from_message = null;
                Message to_message = null;
                Destination from_destination = null;
                if (mobj instanceof com.qbrowser.localstore.LocalMessageContainer) {

                    LocalMessageContainer lmc = (LocalMessageContainer)mobj;

                    from_message = lmc.getMessage();
                    if (from_message == null) {
                        //lazy load
                        from_message = lmc.getRealMessage(session);
                    }

                    try {
                      convertAllLocalDestinationInMessageToVendorDestination(lmc);
                    } catch (Exception converte) {
                        //NOP
                    }
                    MessageContainer tomc = copyMessageContainerForFromLSMessage(lmc);


                    setLocalMessageContainerHeadersToMessageProducer(lmc, sender);


                    to_message = tomc.getMessage();
                    sender.send(tomc.getMessage());

                    if (deleteSrcMessageAfterForward) {
                        //移動の場合は元ネタを消去する(失敗したときは、消されないだけ（暫定）
                        try {

                            String dt_msg = lmc.getVmsgid();
                            LocalMsgTable mt = (LocalMsgTable) fromTable.getModel();
                            int row_no = mt.getRealRowNoFromMsgId(dt_msg);
                            del_targets.add(new Integer(row_no));
                            //実ファイルはここで消す
                            lmc.deleteRealMessageFile();
                        } catch (Throwable thex) {
                            thex.printStackTrace();
                        }
                    }
                } else
                if (mobj instanceof MessageContainer) {

                    MessageContainer frommc = (MessageContainer) mobj;

                    from_message = frommc.getMessage();
                    if (from_message == null) {
                        Queue rq = getQueue(getPureDestName(getPureDestName(from_msg_table_with_suffix)));
                        from_message = frommc.getRealMessageFromBroker(session, rq);
                    }
                    try {
                      convertAllLocalDestinationInMessageToVendorDestination(frommc);
                    } catch (Exception converte) {
                        //NOP
                    }

                    MessageContainer tomc = copyMessageContainer(frommc);

                    from_destination = tomc.getVdest();
                    tomc.setVdest(queue);

                    setHeadersToMessageProducer(from_message, sender);

                    to_message = tomc.getMessage();
                    sender.send(to_message);

                    if (deleteSrcMessageAfterForward) {
                        if (isQueue(from_msg_table_with_suffix)) {
                            //転送元：QUEUE

                            //移動の場合は元ネタを消去する(失敗したときは、消されないだけ（暫定）
                            try {
                                String dselector = "JMSMessageID ='" + from_message.getJMSMessageID() + "'";
                                MessageConsumer mc = session.createConsumer(frommc.getVdest() , dselector, false);
                                Message delm = mc.receive(3000L);
                                DeleteCleanup dcp = new DeleteCleanup();
                                dcp.imc = mc;
                                Thread th1 = new Thread(dcp);
                                th1.start();
                            } catch (Throwable thex) {
                                thex.printStackTrace();
                            }

                        } else if (isTopic(from_msg_table_with_suffix)) {
                            //転送元：TOPIC
                            try {
                                String dt_msg = from_message.getJMSMessageID();
                                MsgTable mt = (MsgTable) fromTable.getModel();
                                int row_no = mt.getRealRowNoFromMsgId(dt_msg);
                                del_tpc_targets.add(new Integer(row_no));
                            } catch (Throwable thex) {
                                thex.printStackTrace();
                            }
                        }
                    }

                }



                mediumbuffer.append(msg057 + " " + to_message.getJMSMessageID() + msg058);
                mediumbuffer.append((i + 1) + msg059 + size + msg060 + "\n");

                //ta.append(mediumbuffer.toString());
                //ta.setCaretPosition(ta.getText().length());
                //mediumbuffer = new StringBuilder();
            }

            sender.close();

            //TPCキャッシュテーブル中からの行削除
            if (del_targets.size() != 0) {
                deleteIndicatedRowsFromLocalMsgTable(from_msg_table_with_suffix , del_targets);
            } else if (del_tpc_targets.size() != 0) {
                deleteIndicatedRowsFromSubscriberCacheTable(from_msg_table_with_suffix , del_tpc_targets);
            }

            ta.append(mediumbuffer.toString());
            ta.setCaretPosition(ta.getText().length());
            ta.append(resources.getString("qkey.msg.msg061"));
            ifnotyetDestNameInQueueDisplayBoxThenAdd(forward_target_name);

        } else if (forward_target_type.equals(TOPIC_LITERAL)) {
            //Topic
            String cjp = getPureDestName(forward_target_name);
            javax.jms.Topic topic = session.createTopic(cjp);
            MessageProducer publisher = session.createProducer(topic);

            TextArea ta = new TextArea("", 10, 90, TextArea.SCROLLBARS_BOTH);
            ta.setEditable(true);
            int size = messages.size();
            int appropriaterowsize = size + 2;
                if (appropriaterowsize > 15) {
                        appropriaterowsize = 15;
                }

             ta.setRows(appropriaterowsize);

            //Topic送信進捗情報

            StringBuffer sb = new StringBuffer();

            if (showMessageDialog) {

                    String dispid = "ForwardMessageProgresDialog";
                    DisplayMsgDialogRunner dpr = new DisplayMsgDialogRunner(dispid ,resources.getString("qkey.msg.msg062"), ta,
                                  QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg), oya_frame);
                    Thread dprth = new Thread(dpr);
                    DisplayDialogThreadPool.addDisplayThread(dpr, dprth);
                    dprth.start();

                    while (!dpr.isStarted()) {
                     try {
                       Thread.sleep(100);
                     } catch (Throwable thex) {}
                     }
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
            String datef = df.format(new Date(System.currentTimeMillis()));
            ta.append(resources.getString("qkey.msg.msg063") + forward_target_name + resources.getString("qkey.msg.msg064") + "\n");
            ta.append(resources.getString("qkey.msg.msg065") + datef + "\n");
            StringBuilder mediumbuffer = new StringBuilder();

            String msg066 = resources.getString("qkey.msg.msg066");
            String msg067 = resources.getString("qkey.msg.msg067");
            String msg068 = resources.getString("qkey.msg.msg068");
            String msg069 = resources.getString("qkey.msg.msg069");


            ArrayList<Integer> del_targets = new ArrayList();
            ArrayList<Integer> del_tpc_targets = new ArrayList();

            for (int i = 0; i < size; i++) {
               Object mobj = messages.get(i);
                Message from_message = null;
                Message to_message   = null;
                Destination from_destination = null;
                //転送元がQueueかTopicの場合

                if (mobj instanceof com.qbrowser.localstore.LocalMessageContainer) {

                    LocalMessageContainer lmc = (LocalMessageContainer)mobj;


                    from_message = lmc.getMessage();
                    if (from_message == null) {
                        from_message = lmc.getRealMessage(session);
                    }
                    try {
                      convertAllLocalDestinationInMessageToVendorDestination(lmc);
                    } catch (Exception converte) {
                        //NOP
                    }
                    MessageContainer tomc = copyMessageContainerForFromLSMessage(lmc);
                    setLocalMessageContainerHeadersToMessageProducer(lmc, publisher);

                    to_message = tomc.getMessage();
                    publisher.send(to_message);

                    if (deleteSrcMessageAfterForward) {
                        //移動の場合は元ネタを消去する(失敗したときは、消されないだけ（暫定）
                        try {

                            String dt_msg = lmc.getVmsgid();
                            LocalMsgTable mt = (LocalMsgTable) fromTable.getModel();
                            int row_no = mt.getRealRowNoFromMsgId(dt_msg);
                            del_targets.add(new Integer(row_no));
                            lmc.deleteRealMessageFile();
                        } catch (Throwable thex) {
                            thex.printStackTrace();
                       }
                    }
                } else
               if (mobj instanceof MessageContainer) {

                    MessageContainer frommc = (MessageContainer) mobj;

                    from_message = frommc.getMessage();
                    if (from_message == null) {
                        Queue rq = getQueue(getPureDestName(getPureDestName(from_msg_table_with_suffix)));
                        from_message = frommc.getRealMessageFromBroker(session, rq);
                    }
                    try {
                      convertAllLocalDestinationInMessageToVendorDestination(frommc);
                    } catch (Exception converte) {
                        //NOP
                    }
                    MessageContainer tomc = copyMessageContainer(frommc);

                    from_destination = tomc.getVdest();
                    tomc.setVdest(topic);

                    setHeadersToMessageProducer(from_message, publisher);

                    to_message = tomc.getMessage();
                    publisher.send(to_message);

                    if (deleteSrcMessageAfterForward) {
                        if (isQueue(from_msg_table_with_suffix)) {
                            //転送元：QUEUE

                            //移動の場合は元ネタを消去する(失敗したときは、消されないだけ（暫定）
                            try {
                                String dselector = "JMSMessageID ='" + from_message.getJMSMessageID() + "'";
                                MessageConsumer mc = session.createConsumer(frommc.getVdest() , dselector, false);
                                Message delm = mc.receive(3000L);
                                DeleteCleanup dcp = new DeleteCleanup();
                                dcp.imc = mc;
                                Thread th1 = new Thread(dcp);
                                th1.start();
                            } catch (Throwable thex) {
                                thex.printStackTrace();
                            }

                        } else if (isTopic(from_msg_table_with_suffix)) {
                            //転送元：TOPIC
                            try {
                                String dt_msg = from_message.getJMSMessageID();
                                MsgTable mt = (MsgTable) fromTable.getModel();
                                int row_no = mt.getRealRowNoFromMsgId(dt_msg);
                                del_tpc_targets.add(new Integer(row_no));
                            } catch (Throwable thex) {
                                thex.printStackTrace();
                            }
                        }
                    }

                }

                mediumbuffer.append(msg066 + to_message.getJMSMessageID() + msg067);
                mediumbuffer.append((i + 1) + msg068 + size + msg069 + "\n");

                //ta.append(mediumbuffer.toString());
                //mediumbuffer = new StringBuilder();
                //ta.setCaretPosition(ta.getText().length());
            }

            publisher.close();

            //TPCキャッシュテーブル中からの行削除
            if (del_targets.size() != 0) {
                deleteIndicatedRowsFromLocalMsgTable(from_msg_table_with_suffix , del_targets);
            } else if (del_tpc_targets.size() != 0) {
                deleteIndicatedRowsFromSubscriberCacheTable(from_msg_table_with_suffix , del_tpc_targets);
            }


            ta.append(mediumbuffer.toString());
            ta.setCaretPosition(ta.getText().length());
            ta.append(resources.getString("qkey.msg.msg070"));
            ifnotyetDestNameInTopicDisplayBoxThenAdd(forward_target_name);
         } else if (forward_target_type.equals(LOCAL_STORE_LITERAL)) {
              //転送ターゲット変数：forward_target_name
              //宛先→ローカルストアへの転送とは、コピーのこと。
              //ただのコピーではなく、メモリ上のメッセージ→一旦ダウンロード
              //ターゲットローカルストアのフォルダへ物理コピー、
              //メモリ上のメッセージもコピーの手順となる。
              LocalStoreManager.LocalStore localstore = lsm.getLocalStoreInstance(forward_target_name);
              TextArea ta = new TextArea("", 10, 90, TextArea.SCROLLBARS_BOTH);
              ta.setEditable(true);
              int size = messages.size();
              int appropriaterowsize = size + 8;
                if (appropriaterowsize > 25) {
                        appropriaterowsize = 25;
                }

              ta.setRows(appropriaterowsize);

              JDialog msgdl = null;

              if (showMessageDialog) {

                    String dispid = "ForwardMessageProgresDialog";
                    DisplayMsgDialogRunner dpr = new DisplayMsgDialogRunner(dispid ,resources.getString("qkey.msg.msg276"), ta,
                                  QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg), oya_frame);

                    Thread dprth = new Thread(dpr);
                    DisplayDialogThreadPool.addDisplayThread(dpr, dprth);
                    dprth.start();

                    while (!dpr.isStarted()) {
                     try {
                       Thread.sleep(100);
                     } catch (Throwable thex) {}
                     }
                    msgdl = dpr.getMessageDialog();
              } else {
                  msgdl = new JDialog();
              }
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
            String datef = df.format(new Date(System.currentTimeMillis()));
            ta.append(resources.getString("qkey.msg.msg278") + forward_target_name + resources.getString("qkey.msg.msg279") + "\n");
            ta.append(resources.getString("qkey.msg.msg280") + datef + "\n");

            StringBuilder mediumbuffer = new StringBuilder();
            String msg057 = resources.getString("qkey.msg.msg281");
            String msg058 = resources.getString("qkey.msg.msg282");
            String msg059 = resources.getString("qkey.msg.msg283");
            String msg060 = resources.getString("qkey.msg.msg284");


            String dest_name_with_suffix =  forward_target_name + LOCAL_STORE_SUFFIX;
            prepareLocalStoreTab(dest_name_with_suffix);
            JTable targetTable = (JTable) jtableins.get(dest_name_with_suffix);
            LocalMsgTable lmt = (LocalMsgTable)targetTable.getModel();

            ArrayList<Integer> del_targets = new ArrayList();
            ArrayList<Integer> del_tpc_targets = new ArrayList();

            for (int i = 0; i < size; i++) {
                Object mobj = messages.get(i);
                String jmsgid = null;
                Message from_message = null;
                Message to_message   = null;
                //Destination from_destination = null;

                //転送元：ローカルストア 転送先：ローカルストア
                if (mobj instanceof com.qbrowser.localstore.LocalMessageContainer) {

                    LocalMessageContainer lmc = (LocalMessageContainer)mobj;

                    from_message = lmc.getMessage();
                    if (from_message == null) {
                        from_message = lmc.getRealMessage(session);
                    }
                    //ローカル→ローカルなので実宛先は必要なし
                    //convertAllLocalDestinationInMessageToVendorDestination(lmc);
                    to_message = copyMessage(from_message);

                    //宛先と送り元が同じ場合は、msgidを変更する
                    String msid = null;
                    long ts = -1;
                    if (dest_name_with_suffix.equals(from_msg_table_with_suffix)) {

                      ts = System.currentTimeMillis();
                      to_message.setJMSTimestamp(ts);
                      msid = "Local_Message" + ts;
                      to_message.setJMSMessageID(msid);

                    }

                    if (lmt != null) {
                      LocalMessageContainer newlmc = new LocalMessageContainer();
                      newlmc.setMessage(to_message);

                      QBrowserUtil.populateHeadersOfLocalMessageContainer(lmc, newlmc);
                      newlmc.setVdest(convertVendorDestinationToLocalDestination(newlmc.getVdest()));
                      newlmc.setVreplyto(convertVendorDestinationToLocalDestination(newlmc.getVreplyto()));
                      newlmc.setDest_name_with_suffix(dest_name_with_suffix);


                      if (msid != null) {
                        newlmc.setVmsgid(msid);
                      }
                      if (ts != -1) {
                        newlmc.setVtimestamp(ts);
                      }

                      lmt.add_one_row_ifexists_update(newlmc);
                      if  (msid != null) {
                          File saved_message = localstore.localMessageToFile(session, newlmc, mediumbuffer, msgdl);
                          LocalStoreManager.addMsgIndex(forward_target_name, newlmc.getVmsgid() , saved_message.getAbsolutePath());
                      } else {

                          File fromf = new File(lmc.getReal_file_path());
                          LocalStoreProperty lsp = lsm.getLocalStoreProperty(forward_target_name);
                          File tof = new File(lsp.getReal_file_directory() + fromf.getName());
                          QBrowserUtil.copy(fromf, tof);
                          LocalStoreManager.addMsgIndex(forward_target_name, newlmc.getVmsgid(), tof.getAbsolutePath());
                      }

                      jmsgid = newlmc.getVmsgid();

                    }

                    //転送元：ローカルストアに対する転送後消し。
                    if (deleteSrcMessageAfterForward) {
                        //移動の場合は元ネタを消去する(失敗したときは、消されないだけ（暫定）
                        try {

                            String dt_msg = lmc.getVmsgid();
                            LocalMsgTable mt = (LocalMsgTable) fromTable.getModel();
                            int row_no = mt.getRealRowNoFromMsgId(dt_msg);
                            del_targets.add(new Integer(row_no));
                            //実ファイル削除
                            lmc.deleteRealMessageFile();
                        } catch (Throwable thex) {
                            thex.printStackTrace();
                        }
                    }
                } else
                //転送元：Queue
                //転送元：Topic
                if (mobj instanceof MessageContainer) {

                    MessageContainer frommc = (MessageContainer) mobj;
                    Message from_m = frommc.getMessage();
                    if (from_m == null) {
                        Queue rq = getQueue(getPureDestName(getPureDestName(from_msg_table_with_suffix)));
                        from_m = frommc.getRealMessageFromBroker(session, rq);
                    }
                    LocalMessageContainer newlmc = new LocalMessageContainer();
                    to_message = copyMessage(from_m);
                    newlmc.setMessage(to_message);
                    QBrowserUtil.populateHeadersOfLocalMessageContainer(frommc , newlmc);

                    newlmc.setVdest(convertVendorDestinationToLocalDestination(newlmc.getVdest()));
                    newlmc.setVreplyto(convertVendorDestinationToLocalDestination(newlmc.getVreplyto() ));
                    newlmc.setDest_name_with_suffix(dest_name_with_suffix);

                    File saved_message = localstore.localMessageToFile(session, newlmc, mediumbuffer, msgdl);
                    LocalStoreManager.addMsgIndex(forward_target_name, from_m.getJMSMessageID() , saved_message.getAbsolutePath());

                    if (lmt != null) {
                      lmt.add_one_row_ifexists_update(newlmc);
                    }

                    jmsgid = newlmc.getVmsgid();

                    if (deleteSrcMessageAfterForward) {
                        if (isQueue(from_msg_table_with_suffix)) {
                            //転送元：QUEUE

                            //移動の場合は元ネタを消去する(失敗したときは、消されないだけ（暫定）
                            try {
                                String dselector = "JMSMessageID ='" + from_m.getJMSMessageID() + "'";
                                MessageConsumer mc = session.createConsumer(convertLocalDestinationToVendorDestination(frommc.getVdest()), dselector, false);
                                Message delm = mc.receive(3000L);
                                DeleteCleanup dcp = new DeleteCleanup();
                                dcp.imc = mc;
                                Thread th1 = new Thread(dcp);
                                th1.start();
                            } catch (Throwable thex) {
                                thex.printStackTrace();
                            }

                        } else if (isTopic(from_msg_table_with_suffix)) {
                            //転送元：TOPIC
                            try {
                                String dt_msg = from_m.getJMSMessageID();
                                MsgTable mt = (MsgTable) fromTable.getModel();
                                int row_no = mt.getRealRowNoFromMsgId(dt_msg);
                                del_tpc_targets.add(new Integer(row_no));
                            } catch (Throwable thex) {
                                thex.printStackTrace();
                            }
                        }
                    }



                }

                mediumbuffer.append(msg057 + " " + jmsgid + msg058);
                mediumbuffer.append((i + 1) + msg059 + size + msg060 + "\n");
                //ta.append(mediumbuffer.toString());
                //ta.setCaretPosition(ta.getText().length());
                //mediumbuffer = new StringBuilder();

            }

            //TPCキャッシュテーブル中からの行削除
            if (del_targets.size() != 0) {
                deleteIndicatedRowsFromLocalMsgTable(from_msg_table_with_suffix , del_targets);
            } else if (del_tpc_targets.size() != 0) {
                deleteIndicatedRowsFromSubscriberCacheTable(from_msg_table_with_suffix , del_tpc_targets);
            }

            refreshLocalStoreMsgTableWithFileReloading(dest_name_with_suffix);

            ta.append(mediumbuffer.toString());
            ta.setCaretPosition(ta.getText().length());
            ta.append(resources.getString("qkey.msg.msg285"));
            //ifnotyetDestNameInQueueDisplayBoxThenAdd(forward_target_name);


         }
        ext_messages = null;

        refreshTableOnCurrentSelectedTab();
    }

    class SubscriberThread extends QBrowserV2.SubscriberThread {

        private SubscriberRunner subscribe_runner;

        public SubscriberThread(SubscriberRunner obj) {
            super(obj);
            this.subscribe_runner = obj;

        }

        @Override
        public void destroy() {

            if (subscribe_runner != null) {
                subscribe_runner.stopSubscribe();
            }
            try {
              super.destroy();
            } catch (Throwable th) {}
        }
    }



    class SubscriberRunner extends QBrowserV2.SubscriberRunner implements Runnable {


        @Override
        public void run() {
            JTable cTable = (JTable) jtableins.get(dest_full_name);
            MsgTable mt = (MsgTable) cTable.getModel();

            try {


            String cjp = getPureDestName(dest_full_name);
            javax.jms.Topic cTopic = session.createTopic(cjp);
            sSubscriber = session.createConsumer(cTopic);

                while (running) {
                    try {
                        Message tmsg = sSubscriber.receive();

                        receive_count++;

                        if (running) {
                            MessageContainer mc = new MessageContainer();
                            mc.setMessage(tmsg);
                            mc.setVdest(convertVendorDestinationToLocalDestination(mc.getVdest()));
                            mc.setVreplyto(convertVendorDestinationToLocalDestination(mc.getVreplyto()));
                            mc.setDest_name_with_suffix(dest_full_name);

                            //追加ヘッダ属性セット
                            ArrayList additional = new ArrayList();
                            org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)tmsg;

                            String jgroupid = acm.getGroupID();
                            if ((jgroupid != null) && (jgroupid.length() != 0)) {
                              Property jgroupidp = new Property();
                              jgroupidp.setKey("Message Group");
                              jgroupidp.setProperty_type(Property.STRING_TYPE);
                              jgroupidp.setProperty_value(jgroupid);
                              additional.add(jgroupidp);
                            }

                            int jgsq = acm.getGroupSequence();

                            if (jgsq != 0) {
                              Property jgsqp = new Property();
                              jgsqp.setKey("Message Group Sequence Number");
                              jgsqp.setProperty_type(Property.INT_TYPE);
                              jgsqp.setProperty_value(jgsq);
                              additional.add(jgsqp);
                            }

                            mc.setAdditionalHeaders(additional);

                            mt.add_one_row(mc);
                            reNumberCTable(cTable);

                            ArrayList local_copy_to = lsm.getCopyToListOfTheDestination(dest_full_name);
                            for (int i = 0 ; i < local_copy_to.size(); i++) {
                                String local_name_without_suffix = (String)local_copy_to.get(i);
                                copyConsumedMessageToLS(local_name_without_suffix, mc);
                                LocalStoreProperty lsp = lsm.getLocalStoreProperty(local_name_without_suffix);
                                lsp.incrementEachCount(dest_full_name);
                            }
                            set_sub_button(dest_full_name);
                        }

                    } catch (Throwable te) {
                        System.err.println("SubscriberRunner while : " + te.getMessage());
                        break;
                      
                    }
                }

                subscribe_thread_status.put(dest_full_name, new Boolean(false));
                set_sub_button(dest_full_name);

            } catch (Throwable gtx) {

                System.err.println(gtx.getMessage());
                subscribe_thread_status.put(dest_full_name, new Boolean(false));
                set_sub_button(dest_full_name);

            }

        }

        @Override
        public void stopSubscribe() {
            running = false;
            if (sSubscriber != null) {
                try {
                    sSubscriber.close();
                } catch (Throwable tex) {
                    //NOP
                }
            }
        }
    }


    class SubscribeOKListener implements ActionListener {

        private boolean checkDups(String hikaku) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) subscribeTemplateBox.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                String key = (String) model.getElementAt(i);
                if (key.trim().equals(hikaku.trim())) {
                    return false;
                }
            }
            return true;
        }

        public void actionPerformed(ActionEvent e) {

            String compl_subscribename = complementTopicName((String)matesakiBox3.getSelectedItem());

            //コピー定義
            String copyto = (String)localstoreBox.getSelectedItem();
            if (!copyto.equals(resources.getString("qkey.msg.msg275"))) {
               try {
                lsm.addDestCopySubscriptionToLocalStore(copyto, compl_subscribename, "");
               } catch (Exception ie) {
                   ie.printStackTrace();
               }
            }

            int current_tab_index = 0;

            //まだタブがないとき
//            if (!isNamedTabAlreadyCreated(compl_subscribename)) {
            if (isNamedTabAlreadyCreated(compl_subscribename)) {
              int target_tab_index = tabbedPane.indexOfTab(compl_subscribename);
              jtableins.remove(compl_subscribename);
              tabbedPane.remove(target_tab_index);
            }

                //先にキャッシュにあるかを判定する
                JTable cTable = (JTable)jtableins.get(compl_subscribename);
                JTable taihiTable = new JTable(new MsgTable());

                //キャッシュにあるからといって、今は停止しているかも
                boolean subscriber_thread_current_running = isSubscriberThreadRunning(compl_subscribename);


                //キャッシュにある場合は、旧データを退避しておく
                if (cTable != null) {
                    tableCopy(cTable, taihiTable);
                }

                //新しいテーブルとタブを作成する
                current_tab_index = createNewMsgPane(compl_subscribename);


                //退避データがあるかどうかをチェック
                if (cTable == null) {
                    //退避データなし/初回なので、スレッドは起動状態で準備
                    cTable = (JTable)jtableins.get(compl_subscribename);
                    MsgTable mt = (MsgTable) cTable.getModel();
                    mt.init();
                    if (subscriber_thread_current_running) {
                        stopSubscriberThread(compl_subscribename);
                    }
                    createAndStartSubscriberThread(compl_subscribename);
                    addDestToMenu(compl_subscribename);
                    DefaultComboBoxModel model = (DefaultComboBoxModel) subscribeTemplateBox.getModel();
                    //重複チェック
                    if (checkDups((String) matesakiBox3.getSelectedItem())) {
                      model.insertElementAt((String) matesakiBox3.getSelectedItem(), 0);
                      subscribeTemplateBox.setSelectedIndex(0);
                    }

                } else {
                    //System.out.println("退避データあり：要復旧");
                    cTable = (JTable)jtableins.get(compl_subscribename);
                    tableCopy(taihiTable, cTable);
                    jtableins.put(compl_subscribename, cTable);
                    restartSubscriberThreadAlongWithCurrentStatus(compl_subscribename);

                }

                tabbedPane.setSelectedIndex(current_tab_index);


//            } else {
//                current_tab_index = tabbedPane.indexOfTab(compl_subscribename);
//                tabbedPane.setSelectedIndex(current_tab_index);
//            }

            subscribeDialog.setVisible(false);
            ifnotyetDestNameInTopicDisplayBoxThenAdd((String)matesakiBox3.getSelectedItem());
            qBox.setSelectedItem(compl_subscribename);
            initTreePane();
            refreshMsgTableWithDestName();

        }
    }

    class ForwardOKListener implements ActionListener {

        boolean deleteSrcMessageAfterForward;

        public ForwardOKListener() {}

        public ForwardOKListener(boolean value) {
            deleteSrcMessageAfterForward = value;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

           try {

           String forward_target_name = (String)matesakiBox2.getSelectedItem();
           String forward_target_type = (String)forwardBox.getSelectedItem();
           int tabindex = tabbedPane.getSelectedIndex();
           String from_msg_table_with_suffix = tabbedPane.getTitleAt(tabindex);
           forwardMessage(ext_messages,from_msg_table_with_suffix,forward_target_name,forward_target_type,deleteSrcMessageAfterForward,true);
           qBox.removeItemListener(acbil);

                    DefaultComboBoxModel model = (DefaultComboBoxModel) qBox.getModel();
                    String compl_dest_name = convertFullDestName(forward_target_name, forward_target_type);
                    boolean found = false;
                    for (int i = 0; i < model.getSize(); i++) {
                        String key = (String) model.getElementAt(i);
                        if (key.trim().equals(compl_dest_name)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        if(!forward_target_type.equals(TOPIC_LITERAL)) {

                          model.insertElementAt(compl_dest_name, 0);
                          qBox.setSelectedItem(compl_dest_name);

                        }
                    } else {
                        //既にBOXに入っていた場合
                        qBox.setSelectedItem(compl_dest_name);
                    }


              refreshMsgTableWithDestName();
              qBox.addItemListener(acbil);
           } catch (Throwable tex) {
               popupErrorMessageDialog(tex);
           }

           forwardDialog.setVisible(false);

        }
    }


    class RefreshDestNames implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
             destinationNamesForDisplayQueue = new ArrayList();
             destinationNamesForDisplayTopic = new ArrayList();
             collectDestination();
             cleanupNewMessagePanelObjects();
             newmessageFrame = null;
             subscribeDialog = null;
             initTreePane();
            } catch (Throwable tex) {
                System.err.println(tex.getMessage());
                //tex.printStackTrace();
            }
        }

    }

    class ReconnectRunner extends QBrowserV2.ReconnectRunner implements Runnable {

        public ReconnectRunner() {
            reconnect_runner_started = true;
        }


        public void run() {
        System.out.println(resourcesamq.getString("qkey.amq.msg.mgs037"));

        try {
        if (session != null) {
            session.close();
            session = null;
        }

        if (connection != null) {

            connection.close();
            connection = null;

        }

        } catch (JMSException jmse) {
            System.err.println(jmse.getMessage());
        }


        setFooter(resourcesamq.getString("qkey.amq.msg.msg009") + " " +
                jmxUrl + "...");

        try {
            initJMS();
            connection.start();
            System.out.println(resourcesamq.getString("qkey.amq.msg.msg010"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            try {

                setFooter(resourcesamq.getString("qkey.amq.msg.msg039"));
                direct_parent.currentThread().sleep(5000);
                run();

            } catch (Exception ie) {
            }
        }
        setFooter(resourcesamq.getString("qkey.amq.msg.mgs038") + " " + jmxUrl);
        }

    }


    @Override
    void reconnect() throws JMSException {
        System.out.println(resourcesamq.getString("qkey.amq.msg.mgs037"));
        if (session != null) {
            session.close();
            session = null;
        }

        if (connection != null) {

            connection.close();
            connection = null;

        }


        setFooter(resourcesamq.getString("qkey.amq.msg.msg040") + " " +
                jmxUrl + "...");

        try {
            initJMS();

            connection.start();
            System.out.println(resourcesamq.getString("qkey.amq.msg.msg039"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            try {

                setFooter(resourcesamq.getString("qkey.amq.msg.msg039"));
                if (!reconnect_runner_started) {

                  Thread recrth = null;
                  ReconnectRunner recr = new ReconnectRunner();
                  recrth = new Thread(recr);
                  recr.direct_parent = recrth;
                  recrth.start();
                }

            } catch (Exception ie) {
            }
        }
        setFooter(resourcesamq.getString("qkey.amq.msg.msg041") + " " + jmxUrl);
    }

    /**
     * Main
     */
    public static void main(String args[]) {

        LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
        try {
            UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
            LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
        } catch (Exception lafe) {
            System.err.println(lafe.getMessage());
            LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
        }

        // スプラッシュの取得
        SplashScreen splash = SplashScreen.getSplashScreen();

        // スプラッシュに描画を行う
        Graphics2D g = splash.createGraphics();
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.setBackground(Color.WHITE);
        g.drawString("ActiveMQ", 29, 60);
        g.drawString("Browser", 29, 80);

        // スプラッシュの更新
        splash.update();


        SwingUtilities.invokeLater(new Runnable() {

            public void run() {


                try {

                    JFrame frame = new JFrame();

                    Dimension d = new Dimension();
                    d.setSize(1080, 700);
                    frame.setPreferredSize(d);
                    frame.setTitle(ActiveMQBrowser.title + " - " + resources.getString("qkey.msg.msg173"));
                    frame.setBackground(Color.white);
                    frame.getContentPane().setLayout(new BorderLayout());

                    final ActiveMQBrowser qb = new ActiveMQBrowser();
                    qb.initLocalStoreManager();
                    qb.initQBrowserKey();

                    qb.setOyaFrame(frame);
                    frame.getContentPane().add("Center", qb);
                    frame.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            qb.cleanupSubscriberThreads();
                            qb.shutdownJMS();
                            System.exit(0);
                        }
                    });
                    frame.pack();
                    JideSwingUtilities.globalCenterWindow(frame);

                    if (isStartingSuccessful) {
                        frame.setVisible(true);
                    }
                    java.net.URL imageURL = QBrowserV2.class.getResource("icons/icons/network16.png");
                    ImageIcon icon = new ImageIcon(imageURL);
                    frame.setIconImage(icon.getImage());
                    qb.tabbedPane.requestFocusForVisibleComponent();

                } catch (Throwable globaltex) {
                    System.err.println(globaltex.getMessage());
                }

            }
        });
    }

    @Override
    public void collectDestination() throws Exception {

        //宛先（Queue,Topic)を全部見つけ出す
        searchQueues();
        searchTopics();


        try {
            String nowselected = (String) qBox.getSelectedItem();

            //Topic退避
            ArrayList taihi = new ArrayList();

            for (int iq = 0; iq < qBox.getItemCount(); iq++) {
                String cdest = (String) qBox.getItemAt(iq);
                if (cdest != null) {
                    if (cdest.indexOf(TOPIC_SUFFIX) != -1) {
                        taihi.add(cdest);
                    }
                }
            }

            qBox.removeItemListener(acbil);
            qBox.removeAllItems();

            // Add sorted names to combo box menu
            //さっき保存した選択済みの名前が新しいリストにあるかチェック
            boolean sakki_found = false;

            Collections.sort(destinationNamesForDisplayQueue);
            Collections.sort(destinationNamesForDisplayTopic);

            for (int i = 0; i < destinationNamesForDisplayQueue.size(); i++) {
                String destfordisp = (String) destinationNamesForDisplayQueue.get(i) + " : Queue";
                addDestToMenu(destfordisp);
                if (destfordisp.equals(nowselected)) {
                    sakki_found = true;
                }


            }

            //LocalStoreここで入れ込み
            ArrayList lsnames = lsm.getAllLocalStoreNames();
            for (int i = 0; i < lsnames.size(); i++) {
                String local_store_name = (String)lsnames.get(i);
                addDestToMenu(local_store_name + LOCAL_STORE_SUFFIX);
            }

            //退避Topicを戻す
            for (int i = 0; i < taihi.size(); i++) {
                String taihied_key = (String) taihi.get(i);
                addDestToMenu(taihied_key);
            }

            qBox.addItemListener(acbil);
            int browseindex = 0;

                if (sakki_found) {
                    browseindex = tabbedPane.indexOfTab(nowselected);
                    if (browseindex == -1) {
                        browseindex = 0;
                    }

                }

            refreshMsgTableWithDestName();

        } catch (Exception e) {
            System.err.println("collectDestination: Exception caught: " + e);
        }
    }



    @Override
    void sendMessage() throws Exception {
        //System.out.println("sendMessage");
        //MQへメッセージを送信する
        //宛先と、メッセージタイプにより作成するメッセージオブジェクトが異なる
        //Text入力→TextMessageクラス
        //File入力→BytesMessageクラス
        //他のメッセージクラスに対応するにはここに追加
        //2009-05 MapMessage追加
        //2009-06 StreamMessage追加
        //2009-06 Message追加

        Message message = null;

        //メッセージタイプ判定
        if (nmi.getBody_inputtype().equals(TEXTMESSAGE)) {

            message = session.createTextMessage(nmi.getBody_text());

        } else if (nmi.getBody_inputtype().equals(BYTESMESSAGE)) {

            //BytesMessage
            BytesMessage bmsg = session.createBytesMessage();

            if (!mfilepath.getText().equals(resources.getString("qkey.msg.msg219"))) {

                java.io.FileInputStream fi = new FileInputStream(nmi.getBody_file());

                byte buf[] = new byte[1024];
                int len = 0;

                int filesizecount = 0;

                while ((len = fi.read(buf)) != -1) {
                    filesizecount += buf.length;
                    bmsg.writeBytes(buf, 0, len);
                }

                fi.close();

            } else {
                //BytesMessageからBytesMessageへ
                //コピーする

                if (passthrough_bytesmessage != null) {

                    passthrough_bytesmessage.reset();

                    byte[] bibi = new byte[1024];
                    int len = 0;
                    long readfilesize = 0;

                    while ((len = passthrough_bytesmessage.readBytes(bibi)) != -1) {
                        bmsg.writeBytes(bibi, 0, len);
                        readfilesize += len;
                    }

                }
            }

            message = bmsg;

        } else if (nmi.getBody_inputtype().equals(MAPMESSAGE)) {
            //MapMessage
            MapMessage mapmsg = session.createMapMessage();
            if (mapm_property_table != null) {

             try {
              for (int i = 0 ; i < mapm_property_table.getRowCount(); i++) {
                Property mapm_body_data = mapm_property_table.getPropertyAtRow(i);

                String key = mapm_body_data.getKey();

                //keyがnullのものについては、未入力と判定する
                if (key != null) {

                switch (mapm_body_data.validated_type) {
                    case Property.INVALID_TYPE_INT:
                       //VALIDではない、セットスキップ
                       break;

                    case Property.PASSTHROUGH_TYPE_INT:
                        mapmsg.setBytes(key, mapm_body_data.getProperty_valueASBytes());
                       break;

                    case Property.STRING_TYPE_INT:
                       mapmsg.setString(key, mapm_body_data.getProperty_valueASString());
                       break;

                    case Property.BOOLEAN_TYPE_INT:
                       mapmsg.setBoolean(key, mapm_body_data.getProperty_valueASBoolean());
                       break;

                    case Property.INT_TYPE_INT:
                       mapmsg.setInt(key, mapm_body_data.getProperty_valueASInt());
                       break;

                    case Property.BYTE_TYPE_INT:
                       mapmsg.setByte(key, mapm_body_data.getProperty_valueASByte());
                       break;

                    case Property.BYTES_TYPE_INT:

                       byte[] bytesarray = QBrowserUtil.extractBytes(mapm_body_data.getProperty_valueASString());
                       if(bytesarray == null) {
                           throw new Exception("Q0021");
                       }
                       mapmsg.setBytes(key, bytesarray);
                       break;

                    case Property.DOUBLE_TYPE_INT:
                       mapmsg.setDouble(key, mapm_body_data.getProperty_valueASDouble());
                       break;

                    case Property.FLOAT_TYPE_INT:
                       mapmsg.setFloat(key, mapm_body_data.getProperty_valueASFloat());
                       break;

                    case Property.LONG_TYPE_INT:
                       mapmsg.setLong(key, mapm_body_data.getProperty_valueASLong());
                       break;

                    case Property.SHORT_TYPE_INT:
                       mapmsg.setShort(key, mapm_body_data.getProperty_valueASShort());
                       break;

                    default :
                       //NOP
                        break;
                }



                }

               } //end for


                } catch (Exception msgstex) {
                    String errmsg = "";
                    TextArea ta = new TextArea("", 5, 50, TextArea.SCROLLBARS_NONE);
                    try {
                    String errmsge = resources.getString("qkey.msg.err." + msgstex.getMessage());
                    ta.append(errmsge);

                    } catch (Exception eex) {}
                    popupMessageDialog(resources.getString("qkey.msg.msg206"), ta,
                                            QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));
                    return;
                }
            }

            message = mapmsg;


        } else if (nmi.getBody_inputtype().equals(STREAMMESSAGE)) {
            //StreamMessage
            StreamMessage smsg = session.createStreamMessage();
            if (sm_property_table != null) {

             try {
              for (int i = 0 ; i < sm_property_table.getRowCount(); i++) {
                StreamMessageInputProperty sm_body_data = sm_property_table.getPropertyAtRow(i);

                String key = String.valueOf(sm_body_data.getSmKey());

                switch (sm_body_data.validated_type) {
                    case Property.INVALID_TYPE_INT:
                       //VALIDではない、セットスキップ
                       break;

                    case Property.PASSTHROUGH_TYPE_INT:
                        smsg.writeBytes(sm_body_data.getProperty_valueASBytes());
                       break;

                    case Property.STRING_TYPE_INT:
                       smsg.writeString(sm_body_data.getProperty_valueASString());
                       break;

                    case Property.BOOLEAN_TYPE_INT:
                       smsg.writeBoolean(sm_body_data.getProperty_valueASBoolean());
                       break;

                    case Property.INT_TYPE_INT:
                       smsg.writeInt(sm_body_data.getProperty_valueASInt());
                       break;

                    case Property.BYTE_TYPE_INT:
                       smsg.writeByte(sm_body_data.getProperty_valueASByte());
                       break;

                    case Property.CHARACTER_TYPE_INT:
                       smsg.writeChar(sm_body_data.getProperty_valueASCharacter());
                       break;

                    case Property.BYTES_TYPE_INT:

                       byte[] bytesarray = QBrowserUtil.extractBytes(sm_body_data.getProperty_valueASString());
                       if(bytesarray == null) {
                           throw new Exception("Q0021");
                       }
                       smsg.writeBytes(bytesarray);
                       break;

                    case Property.DOUBLE_TYPE_INT:
                       smsg.writeDouble(sm_body_data.getProperty_valueASDouble());
                       break;

                    case Property.FLOAT_TYPE_INT:
                       smsg.writeFloat(sm_body_data.getProperty_valueASFloat());
                       break;

                    case Property.LONG_TYPE_INT:
                       smsg.writeLong(sm_body_data.getProperty_valueASLong());
                       break;

                    case Property.SHORT_TYPE_INT:
                       smsg.writeShort(sm_body_data.getProperty_valueASShort());
                       break;

                    default :
                       //NOP
                        break;
                }

               } //end for


                } catch (Exception msgstex) {
                    String errmsg = "";
                    TextArea ta = new TextArea("", 5, 50, TextArea.SCROLLBARS_NONE);
                    try {
                    String errmsge = resources.getString("qkey.msg.err." + msgstex.getMessage());
                    ta.append(errmsge);
                    } catch (Exception eex) {}
                    popupMessageDialog(resources.getString("qkey.msg.msg240"), ta,
                                            QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));
                    return;
                }
            }

            message = smsg;

        } else if (nmi.getBody_inputtype().equals(MESSAGE)) {
            message = session.createMessage();
        }//message作成処理終了

        setDefaultHeaderValue(message);
        setUserPropertyInMessage(message);

        //宛先別に送りを作成
        if (nmi.getDest_type().equals(QUEUE_LITERAL)) {
            //Queue
            //Queue
            String cjp = getPureDestName(nmi.getDest());
            //Queue queue = (Queue) destinationViewObjectCache.get(cjp);
            Queue queue = session.createQueue(cjp);

            
            MessageProducer sender = session.createProducer(queue);


            setHeaderInfoInMessage(sender, message);

            TextArea ta = new TextArea("", 10, 90, TextArea.SCROLLBARS_BOTH);
            ta.setEditable(false);
            ta.setBackground(Color.WHITE);

            int size = nmi.getSoufukosu();

            int appropriaterowsize = size + 3;
            if (appropriaterowsize > 15) {
                appropriaterowsize = 15;
            }

            //Queue送信進捗情報
            ta.setRows(appropriaterowsize);

            String dispid = nmi.toString();
            DisplayMsgDialogRunner dpr = new DisplayMsgDialogRunner(dispid ,resources.getString("qkey.msg.msg053"), ta,
                    QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg), oya_frame);
            Thread dprth = new Thread(dpr);
            DisplayDialogThreadPool.addDisplayThread(dpr, dprth);
            dprth.start();

            while (!dpr.isStarted()) {
                try {
                    Thread.sleep(100);
                } catch (Throwable thex) {}
            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
            String datef = df.format(new Date(System.currentTimeMillis()));
            ta.append(resources.getString("qkey.msg.msg054") + nmi.getDest() + resources.getString("qkey.msg.msg055") + "\n");
            ta.append(resources.getString("qkey.msg.msg056") + datef + "\n");

            //int caretcount = 0;
            StringBuilder mediumbuffer = new StringBuilder();
            String msg057 = resources.getString("qkey.msg.msg057");
            String msg058 = resources.getString("qkey.msg.msg058");
            String msg059 = resources.getString("qkey.msg.msg059");
            String msg060 = resources.getString("qkey.msg.msg060");

            for (int i = 0; i < size; i++) {

                sender.send(message);
                mediumbuffer.append(msg057 + " " + message.getJMSMessageID() + msg058);
                mediumbuffer.append((i + 1) + msg059 + size + msg060 + "\n");

                    //ta.append(mediumbuffer.toString());
                    //ta.setCaretPosition(ta.getText().length());
                    //mediumbuffer = new StringBuilder();
            }


            sender.close();

            ta.append(mediumbuffer.toString());
            ta.setCaretPosition(ta.getText().length());
            ta.append(resources.getString("qkey.msg.msg061"));

            ifnotyetDestNameInQueueDisplayBoxThenAdd(getPureDestName(nmi.getDest()));

        } else if (nmi.getDest_type().equals(TOPIC_LITERAL)) {
            //Topic

            String cjp = getPureDestName(nmi.getDest());
            javax.jms.Topic topic = session.createTopic(cjp);
            MessageProducer publisher = session.createProducer(topic);


            setHeaderInfoInMessage(publisher, message);

            TextArea ta = new TextArea("", 10, 90, TextArea.SCROLLBARS_BOTH);
            ta.setEditable(false);
            ta.setBackground(Color.WHITE);
            int size = nmi.getSoufukosu();

            int appropriaterowsize = size + 3;
            if (appropriaterowsize > 15) {
                appropriaterowsize = 15;
            }

            //Topic送信進捗情報
            ta.setRows(appropriaterowsize);
            StringBuffer sb = new StringBuffer();
            String dispid = nmi.toString();
            DisplayMsgDialogRunner dpr = new DisplayMsgDialogRunner(dispid ,resources.getString("qkey.msg.msg062"), ta,
                    QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg), oya_frame);
            Thread dprth = new Thread(dpr);

            DisplayDialogThreadPool.addDisplayThread(dpr, dprth);
            dprth.start();

            while (!dpr.isStarted()) {
                try {
                    Thread.sleep(100);
                } catch (Throwable thex) {}
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
            String datef = df.format(new Date(System.currentTimeMillis()));
            ta.append(resources.getString("qkey.msg.msg063") + nmi.getDest() + resources.getString("qkey.msg.msg064") + "\n");
            ta.append(resources.getString("qkey.msg.msg065") + datef + "\n");
            StringBuilder mediumbuffer = new StringBuilder();

            String msg066 = resources.getString("qkey.msg.msg066");
            String msg067 = resources.getString("qkey.msg.msg067");
            String msg068 = resources.getString("qkey.msg.msg068");
            String msg069 = resources.getString("qkey.msg.msg069");

            for (int i = 0; i < nmi.getSoufukosu(); i++) {
                publisher.send(message);
                mediumbuffer.append(msg066 + message.getJMSMessageID() + msg067);
                mediumbuffer.append((i + 1) + msg068 + size + msg069 + "\n");

                    //ta.append(mediumbuffer.toString());
                    //ta.setCaretPosition(ta.getText().length());
                    //mediumbuffer = new StringBuilder();

            }

            publisher.close();
            ta.append(mediumbuffer.toString());
            ta.setCaretPosition(ta.getText().length());
            ta.append(resources.getString("qkey.msg.msg070"));

            ifnotyetDestNameInTopicDisplayBoxThenAdd(getPureDestName(nmi.getDest()));
        } else if (nmi.getDest_type().equals(LOCAL_STORE_LITERAL)) {
              //転送ターゲット変数：forward_target_name
              String cjp = getPureDestName(nmi.getDest());
              //TODO
              //宛先→ローカルストアへの転送とは、コピーのこと。
              //ただのコピーではなく、メモリ上のメッセージ→一旦ダウンロード
              //ターゲットローカルストアのフォルダへ物理コピー、
              //メモリ上のメッセージもコピーの手順となる。
              LocalStoreManager.LocalStore localstore = lsm.getLocalStoreInstance(cjp);
              TextArea ta = new TextArea("", 10, 90, TextArea.SCROLLBARS_BOTH);
              ta.setEditable(false);
              ta.setBackground(Color.WHITE);
            int size = nmi.getSoufukosu();

            int appropriaterowsize = size * 8;
            if (appropriaterowsize > 30) {
                appropriaterowsize = 30;
            }

            ta.setRows(appropriaterowsize);

                    String dispid = "ForwardMessageProgresDialog";
                    DisplayMsgDialogRunner dpr = new DisplayMsgDialogRunner(dispid ,resources.getString("qkey.msg.msg276"), ta,
                                  QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.NewMsg), oya_frame);
                    Thread dprth = new Thread(dpr);
                    DisplayDialogThreadPool.addDisplayThread(dpr, dprth);
                    dprth.start();

                    while (!dpr.isStarted()) {
                     try {
                       Thread.sleep(100);
                     } catch (Throwable thex) {}
                     }
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
            String datef = df.format(new Date(System.currentTimeMillis()));
            ta.append(resources.getString("qkey.msg.msg278") + cjp + resources.getString("qkey.msg.msg279") + "\n");
            ta.append(resources.getString("qkey.msg.msg280") + datef + "\n");

            StringBuilder mediumbuffer = new StringBuilder();
            String msg057 = resources.getString("qkey.msg.msg281");
            String msg058 = resources.getString("qkey.msg.msg282");
            String msg059 = resources.getString("qkey.msg.msg283");
            String msg060 = resources.getString("qkey.msg.msg284");

            String dest_name_with_suffix = cjp + LOCAL_STORE_SUFFIX;
            prepareLocalStoreTab(dest_name_with_suffix);
            JTable targetTable = (JTable) jtableins.get(dest_name_with_suffix);
            LocalMsgTable lmt = (LocalMsgTable)targetTable.getModel();

            copyMessageHeaders(nmi.getHeaderinfos(), message);


            for (int i = 0; i < size; i++) {

                    LocalMessageContainer lmc = new LocalMessageContainer();

                    long ts = System.currentTimeMillis();
                    populateHeadersOfLocalMessageContainer(nmi.getHeaderinfos(), lmc);
                    Message to = copyMessage(message);
                    to.setJMSTimestamp(ts);
                    String msid = "Local_Message" + ts;
                    to.setJMSMessageID(msid);
                    LocalQueue lq = new LocalQueue(cjp);
                    to.setJMSDestination(lq);
                    lmc.setMessage(to);
                    lmc.setVreplyto(convertVendorDestinationToLocalDestination(lmc.getVreplyto()));

                    lmc.setVtimestamp(ts);
                    lmc.setVmsgid(msid);
                    lmc.setVdest(lq);


                File saved_message = localstore.localMessageToFile(session, lmc, mediumbuffer, dpr.getMessageDialog());
                LocalStoreManager.addMsgIndex(cjp, lmc.getVmsgid(), saved_message.getAbsolutePath());

                mediumbuffer.append(msg057 + " " + msid + msg058);
                mediumbuffer.append((i + 1) + msg059 + size + msg060 + "\n");
                //ta.append(mediumbuffer.toString());
                //ta.setCaretPosition(ta.getText().length());
                //mediumbuffer = new StringBuilder();

            }

            refreshLocalStoreMsgTableWithFileReloading(dest_name_with_suffix);

            //reNumberLocalCTable(targetTable);


            ta.append(mediumbuffer.toString());
            ta.setCaretPosition(ta.getText().length());
            ta.append(resources.getString("qkey.msg.msg285"));


         }
    }

    String extractUshiro(String orig) {
        if (orig == null) return null;

        int fidx = orig.indexOf("!");
        if (fidx != -1) {
            return orig.substring(fidx + 1);
        } else {
            return orig;
        }
    }

    @Override
    void setHeaderInfoInMessage(MessageProducer pro, Message message) {

        ArrayList jms_headers = nmi.getHeaderinfos();
        boolean isDestinationSpecified = false;
        Destination specifieddestination = null;


        for (int i = 0; i < jms_headers.size(); i++) {
            Property jms_header = (Property) jms_headers.get(i);
            String key = jms_header.getKey();

            try {

                if (key.equalsIgnoreCase("JMSDestination")) {
                    String dest = jms_header.getProperty_valueASString();
                    if (isTopic(dest) || dest.trim().toLowerCase().startsWith("topic://")) {
                        Destination ttdest = session.createTopic(getPureDestName(dest));
                        message.setJMSDestination(ttdest);

                    } else {
                        Destination ttdest = session.createQueue(getPureDestName(dest));
                        message.setJMSDestination(ttdest);
                    }
                } else if (key.equalsIgnoreCase("JMSTimestamp")) {
                } else if (key.equalsIgnoreCase("JMSRedelivered")) {
                } else if (key.equalsIgnoreCase("JMSExpiration")) {
                    message.setJMSExpiration(jms_header.getProperty_valueASInt());
                    pro.setTimeToLive(jms_header.getProperty_valueASInt());
                } else if (key.equalsIgnoreCase("JMSDeliverMode")) {
                    pro.setDeliveryMode(jms_header.getProperty_valueASInt());
                } else if (key.equalsIgnoreCase("JMSType")) {
                    message.setJMSType(jms_header.getProperty_valueASString());
                } else if (key.equalsIgnoreCase("JMSMessageID")) {
                } else if (key.equalsIgnoreCase("JMSCorrelationID")) {
                    message.setJMSCorrelationID(jms_header.getProperty_valueASString());
                } else if (key.equalsIgnoreCase("JMSReplyTo")) {

                    // : Topicがつくかtopic://が頭につくとトピック、それ以外はキュー
                    String reply_dest = jms_header.getProperty_valueASString();

                    if (isTopic(reply_dest) || reply_dest.trim().toLowerCase().startsWith("topic://")) {
                        Destination ttdest = session.createTopic(getPureDestName(reply_dest));
                        message.setJMSReplyTo(ttdest);

                    } else {
                        Destination ttdest = session.createQueue(getPureDestName(reply_dest));
                        message.setJMSReplyTo(ttdest);
                    }


                } else if (key.equalsIgnoreCase("JMSPriority")) {

                    pro.setPriority(jms_header.getProperty_valueASInt());

                } else if (key.equalsIgnoreCase("Message Group Sequence Number")) {

                    org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)message;
                    acm.setGroupSequence(jms_header.getProperty_valueASInt());

                } else if (key.equalsIgnoreCase("Message Group")) {

                    org.apache.activemq.command.Message acm = (org.apache.activemq.command.Message)message;
                    acm.setGroupID(jms_header.getProperty_valueASString());

                }

            } catch (Exception e) {
                //NOP
                e.printStackTrace();
            }

        }
    }

    class DeleteOKListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    deleteconfirmDialog.setVisible(false);
                    TextArea ta = new TextArea("", 10, 90, TextArea.SCROLLBARS_BOTH);
                    ta.setEditable(true);
                    int size = currentDeleteTarget.size();

                    int appropriaterowsize = size + 2;
                    if (appropriaterowsize > 15) {
                        appropriaterowsize = 15;
                    }

                    ta.setRows(appropriaterowsize);

                    //削除進捗情報

                    String dispid = "MessageDeletionProgress";
                    DisplayMsgDialogRunner dpr = new DisplayMsgDialogRunner(dispid ,resources.getString("qkey.msg.msg105"), ta,
                                  QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest), oya_frame);
                    Thread dprth = new Thread(dpr);
                    DisplayDialogThreadPool.addDisplayThread(dpr, dprth);

                    dprth.start();

                    while (!dpr.isStarted()) {
                     try {
                       Thread.sleep(100);
                     } catch (Throwable thex) {}
                     }
                    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
                    String datef = df.format(new Date(System.currentTimeMillis()));
                    ta.append(resources.getString("qkey.msg.msg106") + "\n");
                    ta.append(resources.getString("qkey.msg.msg107") + datef + "\n");
                    //2008-3-31 add transaction-seized message consumption timeout and error.
                    couldnotdelete = new ArrayList();

                    StringBuffer mediumbuffer = new StringBuffer();

                    String msg108 = resources.getString("qkey.msg.msg108");
                    String msg109 = resources.getString("qkey.msg.msg109");
                    String msg110 = resources.getString("qkey.msg.msg110");
                    String msg111 = resources.getString("qkey.msg.msg111");

                    if (!currentDeleteTarget.isEmpty()) {

                        MessageContainer first_one = (MessageContainer)currentDeleteTarget.get(0);
                        String pure_dest_name = first_one.getPureDest_name();

                        QueueViewMBean qvm = (QueueViewMBean)destinationViewObjectCacheForQueue.get(pure_dest_name);


                    int count = 0;
                    String msgid = null;
                    StringBuilder sbuffer = new StringBuilder();
                    for (int i = 0; i < currentDeleteTarget.size(); i++) {
                        try {


                            MessageContainer msg = (MessageContainer) currentDeleteTarget.get(i);
                            msgid = msg.getVmsgid();
                            qvm.removeMessage(msgid);
                            sbuffer.append(msgid + " " + msg108 + (i + 1) + msg109 + size + msg110);

                        } catch (Exception ee) {

                                try {
                                    //接続を張りなおしてリトライ
                                    reconnect();
                                    this.run();
                                } catch (Exception recex) {
                                    //仏の顔は一度まで。
                                    couldnotdelete.add(msgid);
                                }

                        }
                    } //roop end

                    ta.append(sbuffer.toString());
                    sbuffer = null;
                    ta.setCaretPosition(ta.getText().length());

                    }

                    if (couldnotdelete.isEmpty()) {

                        ta.append(resources.getString("qkey.msg.msg112"));

                    } else {
                        ta.append(resources.getString("qkey.msg.msg113") + "\n");
                        ta.append(resources.getString("qkey.msg.msg114") + "\n");
                        for (int j = 0; j < couldnotdelete.size(); j++) {
                            String cmsgid = (String) couldnotdelete.get(j);
                            ta.append(cmsgid + "\n");
                        }
                    }

                    //doBrowse();
                    currentDeleteTarget.clear();
                    refreshTableOnCurrentSelectedTab();
                }
            });


        }
    }

    class NewMessageOKListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {


            //新メッセージ作成処理開始！

            //宛先名取得
            String dest_name = (String) matesakiBox1.getSelectedItem();
            //V2連絡エリア
            matesakiname = new JTextField();
            matesakiname.setText(dest_name);

            //宛先タイプ取得
            String dest_type = (String) mqBox.getSelectedItem();

            //body入力タイプ取得
            String bodyinputtype = (String)message_type.getSelectedItem();

            //入力タイプ別にボディ情報を入手
            if (bodyinputtype.equals(TEXTMESSAGE)) {
                //Text
                String data = mbodyPanel.textArea.getText();
            } else if (bodyinputtype.equals(BYTESMESSAGE)) {
                //File

                if (!mfilepath.getText().equals(resources.getString("qkey.msg.msg219"))) {

                    File ff = new File(mfilepath.getText());
                    if (!ff.exists()) {
                        cmessagefooter.setText(resources.getString("qkey.msg.msg079"));
                        return;
                    } else if (ff.isDirectory()) {
                        cmessagefooter.setText(resources.getString("qkey.msg.msg080"));
                        return;
                    }

                }
            }

            try {

                Integer.parseInt(soufukosu.getText().trim());

            } catch (Exception nfe) {

                cmessagefooter.setText(resources.getString("qkey.msg.msg081"));
                return;

            }

            //フラグリセット
            newmessage1stpanelok = true;
            newmessage1stpanel_user_props_ok = true;
            newmessage1stpanel_mapm_props_ok = true;
            newmessage1stpanel_sm_props_ok = true;

            last_jmsheader_validate_error = "";
            last_user_prop_validate_error = "";
            last_mapmessage_prop_validate_error = "";
            last_streammessage_prop_validate_error = "";



            hdce2.stopCellEditing();
            pdce1.stopCellEditing();
            pdce3.stopCellEditing();

            validateAllUserProperties();

            //エディットの確定とvalidate
            if (bodyinputtype.equals(MAPMESSAGE)) {
              if (mapmdce0 != null)
                mapmdce0.stopCellEditing();
              if (mapmdce3 != null)
                mapmdce3.stopCellEditing();
            } else if (bodyinputtype.equals(STREAMMESSAGE)) {
              if (smdce3 != null)
                smdce3.stopCellEditing();
            }

            //JMSヘッダのチェックOK
            if (newmessage1stpanelok) {

                //道のりは遠い・・・ユーザプロパティチェックOK
                //MAPMESSAGEの時はMAPMESSAGEプロパティチェックに通っていること
                if (newmessage1stpanel_user_props_ok) {

                    if (newmessage1stpanel_mapm_props_ok) {
                        if (newmessage1stpanel_sm_props_ok) {
                            showMessageSendConfirmation(bodyinputtype);
                        } else {

                        ArrayList ar = QBrowserUtil.parseDelimitedString(last_streammessage_prop_validate_error, MAGIC_SEPARATOR);
                        String errorcode = null;
                        String errorprop = null;
                        String errortype = null;
                        String errorvalue = null;

                        int count = 0;
                        for (int i = 0; i < ar.size(); i++) {
                            count++;
                            switch (count) {
                                case 1:
                                    errorcode = (String) ar.get(i);
                                case 2:
                                    errorprop = (String) ar.get(i);
                                case 3:
                                    errortype = (String) ar.get(i);
                                case 4:
                                    errorvalue = (String) ar.get(i);

                            }
                        }


                        cmessagefooter.setText(resources.getString("qkey.msg.msg178"));
                        TextArea ta = new TextArea("", 7, 50, TextArea.SCROLLBARS_BOTH);
                        ta.append(resources.getString("qkey.msg.msg237"));
                        ta.append(resources.getString("qkey.msg.err." + errorcode));
                        ta.append(errorprop + " (" + errortype + " " + resources.getString("qkey.msg.msg182") + ") = " + errorvalue + "\n");

                        popupMessageDialog(resources.getString("qkey.msg.msg178"), ta, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));


                        }
                    } else {
                        ArrayList ar = QBrowserUtil.parseDelimitedString(last_mapmessage_prop_validate_error, MAGIC_SEPARATOR);
                        String errorcode = null;
                        String errorprop = null;
                        String errortype = null;
                        String errorvalue = null;

                        int count = 0;
                        for (int i = 0; i < ar.size(); i++) {
                            count++;
                            switch (count) {
                                case 1:
                                    errorcode = (String) ar.get(i);
                                case 2:
                                    errorprop = (String) ar.get(i);
                                case 3:
                                    errortype = (String) ar.get(i);
                                case 4:
                                    errorvalue = (String) ar.get(i);

                            }
                        }


                        cmessagefooter.setText(resources.getString("qkey.msg.msg178"));
                        TextArea ta = new TextArea("", 7, 50, TextArea.SCROLLBARS_BOTH);
                        ta.append(resources.getString("qkey.msg.msg226"));
                        ta.append(resources.getString("qkey.msg.err." + errorcode));
                        ta.append(errorprop + " (" + errortype + " " + resources.getString("qkey.msg.msg182") + ") = " + errorvalue + "\n");

                        popupMessageDialog(resources.getString("qkey.msg.msg178"), ta, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));

                    }

                } else {
                    ArrayList ar = QBrowserUtil.parseDelimitedString(last_user_prop_validate_error, MAGIC_SEPARATOR);
                    String errorcode = null;
                    String errorprop = null;
                    String errortype = null;
                    String errorvalue = null;

                    int count = 0;
                    for (int i = 0; i < ar.size(); i++) {
                        count++;
                        switch (count) {
                            case 1:
                                errorcode = (String) ar.get(i);
                            case 2:
                                errorprop = (String) ar.get(i);
                            case 3:
                                errortype = (String) ar.get(i);
                            case 4:
                                errorvalue = (String) ar.get(i);

                        }
                    }


                    cmessagefooter.setText(resources.getString("qkey.msg.msg178"));
                    TextArea ta = new TextArea("", 7, 50, TextArea.SCROLLBARS_BOTH);
                    ta.append(resources.getString("qkey.msg.msg180"));
                    ta.append(resources.getString("qkey.msg.err." + errorcode));
                    ta.append(errorprop + " (" + errortype + " " + resources.getString("qkey.msg.msg182") + ") = " + errorvalue + "\n");

                    popupMessageDialog(resources.getString("qkey.msg.msg178"), ta, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));

                }
            } else {
                //エラー構文
                ArrayList ar = QBrowserUtil.parseDelimitedString(last_jmsheader_validate_error, MAGIC_SEPARATOR);
                String errorcode = null;
                String errorprop = null;
                String errortype = null;
                String errorvalue = null;

                int count = 0;
                for (int i = 0; i < ar.size(); i++) {
                    count++;
                    switch (count) {
                        case 1:
                            errorcode = (String) ar.get(i);
                        case 2:
                            errorprop = (String) ar.get(i);
                        case 3:
                            errortype = (String) ar.get(i);
                        case 4:
                            errorvalue = (String) ar.get(i);

                    }
                }

                cmessagefooter.setText(resources.getString("qkey.msg.msg181"));
                TextArea ta = new TextArea("", 5, 50, TextArea.SCROLLBARS_NONE);
                ta.append(resources.getString("qkey.msg.msg179"));
                ta.append(resources.getString("qkey.msg.err." + errorcode));
                ta.append(errorprop + " = " + errorvalue + "\n");

                popupMessageDialog(resources.getString("qkey.msg.msg178"), ta, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));

            }

        }
    }


    private static String truncate(String s)
    {
        String s1 = s;
        if(s.length() >= 30)
        {
            return s.substring(0, 30);
        } else
        {
            return StringUtils.padStringWidth(s1, 30);
        }
    }

class QBrowserKey
{

    private final Properties props = new Properties();
    public final String KEY_STROKE_FILE = "keystrokeamq";

    public QBrowserKey()
    {
        String packageName = getCallerPackage();
        String location;
        if(OSDetector.isMac())
        {
            //暫定的にwinと同じにしておく。
            location = packageName + "/" + "keystrokeamq" + "_win.properties";
        } else
        {
            location = packageName + "/" + "keystrokeamq" + "_win.properties";
        }
        java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(location);
        if(null != is)
        {
            try
            {
                props.load(is);
            }
            catch(IOException ioe) { }
        }
    }

    public void setKeyStroke(JMenuItem menuItem, String key)
    {
        String property = getProperty(key);
        if(null != property && !"".equals(property))
        {
            if(property.startsWith("_"))
            {
                menuItem.setMnemonic(property.charAt(1));
                if(-1 == menuItem.getText().toUpperCase().indexOf(property.charAt(1)))
                {
                    menuItem.setText(menuItem.getText() + "(" + property.charAt(1) + ")");
                }
            } else
            {
                menuItem.setAccelerator(toKeyStroke(property));
            }
        }
    }


    public void setKeyStroke(JMenu menuItem, String key)
    {
        String property = getProperty(key);
        if(null != property && !"".equals(property))
        {

                menuItem.setMnemonic(property.charAt(1));
                if(-1 == menuItem.getText().toUpperCase().indexOf(property.charAt(1)))
                {
                    menuItem.setText(menuItem.getText() + "(" + property.charAt(1) + ")");
                }
        }
    }

    private String getCallerPackage()
    {
        String callerClass = (new Throwable()).getStackTrace()[2].getClassName();
        return callerClass.substring(0, callerClass.lastIndexOf(".")).replaceAll("\\.", "/");
    }

    public char getMnemonic(String key)
    {
        return getProperty(key).charAt(0);
    }

    public KeyStroke getKeyStroke(String key)
    {
        return toKeyStroke(getProperty(key));
    }

    public String getProperty(String key)
    {
        return props.getProperty(key);
    }

    public KeyStroke toKeyStroke(String keyStrokeText)
    {
        if(null != keyStrokeText)
        {
            keyStrokeText = keyStrokeText.replaceAll("command", "meta");
            keyStrokeText = keyStrokeText.replaceAll("cmd", "meta");
            keyStrokeText = keyStrokeText.replaceAll("option", "alt");
            keyStrokeText = keyStrokeText.replaceAll("ctl", "control");
            keyStrokeText = keyStrokeText.replaceAll("ctrl", "control");
            keyStrokeText = keyStrokeText.replaceAll("opt", "alt");
        }
        return KeyStroke.getKeyStroke(keyStrokeText);
    }

    public void apply(Object obj)
    {
        Field fields[] = obj.getClass().getFields();


        for(int i = 0; i < fields.length; i++)
        {
            String fieldName = fields[i].getName();
            Class type = fields[i].getType();
            try
            {
                Object theObject = fields[i].get(obj);

                if (theObject instanceof JMenu) {
                    JMenu menu = (JMenu)theObject;
                    String key = menu.getText();
                    if(key.indexOf("qkey.amq") != -1) {
                      menu.setText(resourcesamq.getString(key));
                    } else {
                      menu.setText(resources.getString(key));
                    }
                    setKeyStroke(menu, key);
                } else
                if(theObject instanceof JMenuItem)
                {
                    JMenuItem menuItem = (JMenuItem)theObject;
                    String key = menuItem.getText();
                    if(key.indexOf("qkey.amq") != -1) {
                       menuItem.setText(resourcesamq.getString(key));
                    } else {
                       menuItem.setText(resources.getString(key));
                    }
                    setKeyStroke(menuItem, key);
                }
            }
            catch(IllegalAccessException iae) { iae.printStackTrace(); }
        }


    }

    public boolean isPressed(String key, KeyEvent event)
    {
        return getKeyStroke(key).equals(KeyStroke.getKeyStrokeForEvent(event));
    }

    public boolean isPressed(String key1, String key2, KeyEvent event)
    {
        return isPressed(key1, event) || isPressed(key2, event);
    }
}


    class SearchOKListener implements ActionListener {

        QBrowserV2 qbv2;
        MsgTable mt;

        public SearchOKListener(QBrowserV2 oya) {
            qbv2 = oya;
        }

        private boolean checkDups(String hikaku) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) searchTemplateBox.getModel();
            for (int i = 0 ; i < model.getSize(); i++ ) {
                String key = (String) model.getElementAt(i);
                if (key.trim().equals(hikaku.trim())) {
                    return false;
                }
            }
            return true;
        }

        public void actionPerformed(ActionEvent e) {
            if (searchtextfield == null) {
                selector = null;
            } else if (searchtextfield.getText().length() == 0) {
                selector = null;
            } else {
                selector = searchtextfield.getText();
                DefaultComboBoxModel model = (DefaultComboBoxModel) searchTemplateBox.getModel();

                if (checkDups(searchtextfield.getText())) {
                    model.insertElementAt(searchtextfield.getText(), 0);
                    searchTemplateBox.setSelectedIndex(0);
                }

                //ファイルに書き出すものについては、今回のコマンドならば重複チェックなし。
                QBrowserUtil.saveHistoryToFile("search_history_amq", QBrowserUtil.jcomboBoxToArrayList(searchTemplateBox));
            }

            String selectedDest = (String) tqBox.getSelectedItem();


            //宛先が空欄の場合は全部のキューから探してきて、件数のみ表示する
            //Selector文字列は空白でない場合のみ。
            if (((selectedDest == null) || (selectedDest.length() == 0)) && ((selector != null) && (selector.length() != 0))) {


                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        TextArea ta = new TextArea("", 10, 59, TextArea.SCROLLBARS_VERTICAL_ONLY);
                        ta.setEditable(true);

                        //popupMessageDialog("メッセージ検索結果", ta);
                        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss z");
                        ArrayList resultcol = new ArrayList();
                        int total_found = 0;

                        for (int i = 0; i < tqBox.getItemCount(); i++) {
                            String tqdest =  getPureDestName((String) tqBox.getItemAt(i));

                            int kensu = searchBrowse(tqdest, selector);

                            if (kensu != 0) {
                                total_found += kensu;
                                String found = tqdest + " " + resources.getString("qkey.msg.msg085") + " " + kensu + " " + resources.getString("qkey.msg.msg086") + "\n";
                                ta.append(found);
                                resultcol.add(found);
                                for (int j = 0; j < kensu; j++) {
                                    MessageContainer msg = mt.getMessageAtRow(j);
                                    ta.append("  " + msg.getVmsgid() + "\n");

                                }

                            }
                        }

                        ta.append(resources.getString("qkey.msg.msg087") + "\n");
                        ta.append(resources.getString("qkey.msg.msg088"));
                        ta.append(resources.getString("qkey.msg.msg089") + selector + "\n\n");
                        for (int k = 0; k < resultcol.size(); k++) {
                            ta.append((String) resultcol.get(k));
                        }
                        ta.append(resources.getString("qkey.msg.msg090") + " " + total_found + " " + resources.getString("qkey.msg.msg091") + "\n");
                        ta.append(resources.getString("qkey.msg.msg092"));
                        popupMessageDialog(resources.getString("qkey.msg.msg093"), ta,
                                 QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.AtesakiDetails));
                        ta.setCaretPosition(ta.getText().length());

                    }
                });


            } else {
                ComboBoxEditor editor = qBox.getEditor();
                editor.setItem(selectedDest);
                doBrowse();
            }
            searchDialog.setVisible(false);
        }

       int searchBrowse(String destname, String sel) {
            int returnvalue = 0;
            try {
            Queue q = session.createQueue(destname);

            QueueBrowser qb;
            qb = session.createBrowser(q, sel);

            mt = new MsgTable();
            Enumeration emt = qb.getEnumeration();
            ArrayList tc = new ArrayList();
            while (emt.hasMoreElements()) {
                Message imsg = (Message)emt.nextElement();
                MessageContainer mc = new MessageContainer();
                mc.setMessage(imsg);
                mc.setDest_name_with_suffix(destname + QUEUE_SUFFIX);

                try {
                 mc.setVdest(convertVendorDestinationToLocalDestination(imsg.getJMSDestination()));
                 mc.setVreplyto(convertVendorDestinationToLocalDestination(imsg.getJMSReplyTo()));
                } catch (Exception mce) { mce.printStackTrace();}
                tc.add(mc);
            }
            returnvalue = mt.load(tc);
            qb.close();
            } catch (JMSException jmse) {
                //NOP
                //jmse.printStackTrace();
            }

            return returnvalue;
        }
    }


    class SendAtesakiComboBoxItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            
            if (e.getStateChange() == e.SELECTED) {

            String sel = (String)mqBox.getSelectedItem();
            if (sel.equals(TOPIC_LITERAL)) {
                 importTopicNamesToMATESAKIBOX1();
                 matesakiBox1.setEditable(true);
            } else if (sel.equals(QUEUE_LITERAL)) {
                 importQueueNamesToMATESAKIBOX1();
                 matesakiBox1.setEditable(true);
            } else if (sel.equals(LOCAL_STORE_LITERAL)) {
                 importLocalStoreNamesToMATESAKIBOX1();
            }

            }
        }

    }

    class SendForwardAtesakiComboBoxItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == e.SELECTED) {
            String sel = (String)forwardBox.getSelectedItem();
            if (sel.equals(TOPIC_LITERAL)) {
                 importTopicNamesToMATESAKIBOX2();
                 matesakiBox2.setEditable(false);
            } else if (sel.equals(QUEUE_LITERAL)) {
                 importQueueNamesToMATESAKIBOX2();
                 matesakiBox2.setEditable(false);
            } else if (sel.equals(LOCAL_STORE_LITERAL)) {
                 importLocalStoreNamesToMATESAKIBOX2();
            }
            }
        }

    }

    class ConnectionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {


            showConnectionWindow();

        }
    }

    @Override
    public void moveToDisconnectStatus() {
            cleanupQBrowser();
            setNotConnected();
            connected = false;
            disconnect_item.setEnabled(false);
            connect_item.setEnabled(true);
            oya_frame.setTitle(ActiveMQBrowser.title + " - " + resources.getString("qkey.msg.msg173"));
            setFooter(resourcesamq.getString("qkey.amq.msg.msg043"));
            tree_location.remove(treePane);
            tree_location.updateUI();
    }

    class DisConnectionListener extends QBrowserV2.DisConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

           moveToDisconnectStatus();

        }
    }

     class ConnectionTemplateItemListener implements ItemListener {

        private void parseTemplateString() {
            String selected = (String) connectionTemplateBox.getSelectedItem();
            //"host = localhost port = 7676 user = admin password = admin "
            try {
                StringTokenizer st = new StringTokenizer(selected);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.equals("jmxUrl")) {
                        if (st.nextToken().equals("=")) {
                            connectiontext_jmxurl.setText(st.nextToken());
                        }
                    } else if (token.equals("role")) {
                        if (st.nextToken().equals("=")) {
                            connectiontext_role.setText(st.nextToken());
                        }
                    } else if (token.equals("password")) {
                        if (st.nextToken().equals("=")) {
                            connectiontext_password.setText(st.nextToken());
                        }
                    }

                }
            } catch (Throwable txex) {
                //Parse失敗、何もしない。
            }
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == e.SELECTED) {
                  parseTemplateString();
            }
        }
    }

   class ConnectionOKListener implements ActionListener {


        public ConnectionOKListener() {

        }

        private boolean checkDups(String hikaku) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) connectionTemplateBox.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                String key = (String) model.getElementAt(i);
                if (key.trim().equals(hikaku.trim())) {
                    model.removeElementAt(i);
                    return false;
                }
            }
            return true;
        }

        //パスワードは履歴にいれちゃいます。
        private String generateTemplateString(String jmxUrl, String role, String password) {
            //"host = localhost port = 7676 user = admin password = admin "

            if (role != null) {
              return "jmxUrl = " + jmxUrl + " role = " + role + " password = " + password;
            } else {
              return "jmxUrl = " + jmxUrl;
            }

        }

        public void actionPerformed(ActionEvent e) {
            if (connectiontext_jmxurl != null) {

                String new_jmxurl = connectiontext_jmxurl.getText();
                String new_role = connectiontext_role.getText();
                String new_password = new String(connectiontext_password.getPassword());
                //ここに再接続ロジックを書く。
                jmxUrl = new_jmxurl;

                try {

                if ((new_role == null) || (new_role.trim().length() == 0)) {
                    new_role = DEFAULT_BROKER_ADMIN_ROLE;
                }

                if ((new_password == null) || (new_password.trim().length() == 0)) {
                    new_password = DEFAULT_BROKER_PASSWORD;
                }

                jmxRole = new_role;
                jmxPassword = new_password;

                try {
                    connect();
                } catch (Exception conex) {
                    TextArea ta = new TextArea("", 13, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);

                    ta.append(resources.getString("qkey.msg.msg149"));
                    ta.append(resourcesamq.getString("qkey.amq.msg.msg014"));
                    ta.append(resourcesamq.getString("qkey.amq.msg.msg015"));
                    ta.append(resourcesamq.getString("qkey.amq.msg.msg016") + " " + jmxUrl + "\n\n");
                    ta.append(resourcesamq.getString("qkey.amq.msg.msg017"));

                    ta.append(conex.getMessage());
                    popupMessageDialog(resources.getString("qkey.msg.msg170"),ta,
                            QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.EXIT));

                    return;
                }

                    bkr_instance_name =  bf.getBrokerAdmin().getBrokerName();
                    

                    initTreePane();


                //履歴にいれましょ。
                DefaultComboBoxModel model = (DefaultComboBoxModel) connectionTemplateBox.getModel();
                //重複チェック
                String generatedTemplateString = generateTemplateString(jmxUrl, new_role, new_password);
                checkDups(generatedTemplateString);
                model.insertElementAt(generatedTemplateString, 0);
                connectionTemplateBox.setSelectedIndex(0);


                QBrowserUtil.saveHistoryToFile("connect_history_amq", QBrowserUtil.jcomboBoxToArrayList(connectionTemplateBox));

                //doBrowse();
                setConnected();
                connected = true;
                //System.out.println("処理");
                oya_frame.setTitle(ActiveMQBrowser.title + " - " + bkr_instance_name + "(" + jmxUrl + ")   role=" + jmxRole);
                disconnect_item.setEnabled(true);
                connect_item.setEnabled(false);


                } catch (Exception nfe) {
                    popupErrorMessageDialog(nfe);
                }
            }

            connectionDialog.setVisible(false);
        }

    }


    class ConnectionCancelListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            connectionDialog.setVisible(false);

        }
    }

    class VersionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

           StringBuilder sb = new StringBuilder();
           String ver = resourcesamq.getString("qkey.amq.msg.mgs001");

           sb.append("Written by Naoki Takemura\nPlease send feedback to naoki_takemura@hotmail.com\n");
           sb.append(ver).append(" ");
           sb.append(ActiveMQConnectionMetaData.PROVIDER_VERSION);



           JTextArea ta = new JTextArea();
           ta.setText(sb.toString());

           popupMessageDialog(ver, createSearchableTextArea(ta),
                      QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.ClientVersion));

        }
    }


    class PurgeQueueListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            matesakiBox1 = new JComboBox();
            importQueueNamesToMATESAKIBOX1();
            matesakiBox1.setEditable(false);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resourcesamq.getString("qkey.amq.msg.msg045"));
            panel.add(BorderLayout.NORTH, lbl);
            panel.add(BorderLayout.CENTER, matesakiBox1);

            popupConfirmationDialog(resourcesamq.getString("qkey.amq.msg.msg044"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String selectedQueue = (String)matesakiBox1.getSelectedItem();
                            if (selectedQueue != null && selectedQueue.length() != 0) {
                                try {
                                Queue target_queue = session.createQueue(selectedQueue);
                                bf.purgeQueue((ActiveMQDestination)target_queue);
                                qBox.setSelectedItem(selectedQueue + QUEUE_SUFFIX);
                                refreshMsgTableWithDestName();
                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }

    
    class PurgeQueueForPopupListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

        TextArea ta = new TextArea("", 2, 40, TextArea.SCROLLBARS_NONE);

        int selidx = tabbedPane.getSelectedIndex();
        final String name = getPureDestName(tabbedPane.getTitleAt(selidx));
        ta.append(resources.getString("qkey.msg.msg011"));
        ta.append(name);
        ta.append(resources.getString("qkey.msg.msg012"));

        ta.setColumns(30 + name.length());
        ta.setEditable(false);
        ta.setBackground(Color.WHITE);

            JPanel panel = new JPanel();
            panel.add(ta);

            popupConfirmationDialog(resourcesamq.getString("qkey.amq.msg.msg044"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.PurgeDest),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;

                                try {
                                Queue target_queue = session.createQueue(name);
                                bf.purgeQueue((ActiveMQDestination)target_queue);
                                qBox.setSelectedItem(name + QUEUE_SUFFIX);
                                refreshMsgTableWithDestName();
                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            

                        }
                    });



        }
    }


    class DeleteQueueListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            matesakiBox1 = new JComboBox();
            importQueueNamesToMATESAKIBOX1();
            matesakiBox1.setEditable(false);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resourcesamq.getString("qkey.amq.msg.msg053"));
            panel.add(BorderLayout.NORTH, lbl);
            panel.add(BorderLayout.CENTER, matesakiBox1);

            popupConfirmationDialog(resourcesamq.getString("qkey.amq.msg.msg052"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String selectedQueue = (String)matesakiBox1.getSelectedItem();
                            if (selectedQueue != null && selectedQueue.length() != 0) {
                                try {
                                  DestinationFacade df = new DestinationFacade(bf);
                                  df.setJMSDestination(selectedQueue);
                                  df.setJMSDestinationType("queue");
                                  df.removeDestination();
                                  
                                  //パネルが存在している場合クローズ

                                  String dest_with_suffix = selectedQueue + QUEUE_SUFFIX;
                                  removeNamedTabbedPane(dest_with_suffix);
                                  int selidx = tabbedPane.getSelectedIndex();
                                  if (selidx != -1) {
                                    refreshTableOnCurrentSelectedTab();
                                     String tabname = tabbedPane.getTitleAt(selidx);
                                     qBox.setSelectedItem(tabname);
                                  }
                                  removeDestRelatedCache(dest_with_suffix);
                                  destinationNamesForDisplayQueue = new ArrayList();
                                  destinationNamesForDisplayTopic = new ArrayList();
                                  collectDestination();
                                  initTreePane();
                                  refreshMsgTableWithDestName();
 
                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }

    class DeleteTopicListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            matesakiBox1 = new JComboBox();
            importTopicNamesToMATESAKIBOX1();
            matesakiBox1.setEditable(false);
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resourcesamq.getString("qkey.amq.msg.msg055"));
            panel.add(BorderLayout.NORTH, lbl);
            panel.add(BorderLayout.CENTER, matesakiBox1);

            popupConfirmationDialog(resourcesamq.getString("qkey.amq.msg.msg054"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String selectedTopic = (String)matesakiBox1.getSelectedItem();
                            if (selectedTopic != null && selectedTopic.length() != 0) {
                                try {
                                  DestinationFacade df = new DestinationFacade(bf);
                                  df.setJMSDestination(selectedTopic);
                                  df.setJMSDestinationType("topic");
                                  df.removeDestination();
                                  //パネルが存在している場合クローズ
                                 

                                  String dest_with_suffix = selectedTopic + TOPIC_SUFFIX;
                                  removeNamedTabbedPane(dest_with_suffix);

                                  int selidx = tabbedPane.getSelectedIndex();
                                  if (selidx != -1) {
                                    refreshTableOnCurrentSelectedTab();
                                     String tabname = tabbedPane.getTitleAt(selidx);
                                     qBox.setSelectedItem(tabname);
                                  }
                                  removeDestRelatedCache(dest_with_suffix);
                                  stopSubscriberThread(dest_with_suffix);
                                  subscribe_thread_status.remove(dest_with_suffix);

                                  //関連するローカルストアからこのトピック名を削除
                                  ArrayList local_copy_to = lsm.getCopyToListOfTheDestination(dest_with_suffix);
                                  for (int i = 0 ; i < local_copy_to.size(); i++) {
                                      String local_name_without_suffix = (String)local_copy_to.get(i);
                                      LocalStoreProperty lsp = lsm.getLocalStoreProperty(local_name_without_suffix);
                                      lsp.removeFromDests(dest_with_suffix);
                                      lsm.updateAndSaveLocalStoreProperty(lsp);
                                  }
                                  lsm.removeRelatedEntryOfSubscribeDest(dest_with_suffix);

                                  destinationNamesForDisplayQueue = new ArrayList();
                                  destinationNamesForDisplayTopic = new ArrayList();
                                  collectDestination();
                                  initTreePane();
                                  refreshMsgTableWithDestName();

                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }

    class CreateTopicForTreeListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            final JTextField topic_name_input = new JTextField();
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resources.getString("qkey.msg.msg349"));
            panel.add(BorderLayout.NORTH, lbl);
            panel.add(BorderLayout.CENTER, topic_name_input);

            popupConfirmationDialog(resources.getString("qkey.msg.msg348"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String target_topic_name = topic_name_input.getText();
                            if (target_topic_name != null && target_topic_name.length() != 0) {
                                try {
                                  
                                    
                                  bf.getBrokerAdmin().addTopic(target_topic_name);
                                  destinationNamesForDisplayQueue = new ArrayList();
                                  destinationNamesForDisplayTopic = new ArrayList();
                                  collectDestination();
                                  //DUMMY
                                  matesakiBox3 = new JComboBox();
                                  importTopicNamesToMATESAKIBOX3();
                                  matesakiBox3.setSelectedItem(target_topic_name);
                                  localstoreBox = new JComboBox();
                                  subscribeTemplateBox = new JComboBox();
                                  subscribeDialog = new JDialog();
                                  importLocalStoreNamesToLOCALSTOREBOX();
                                  localstoreBox.setSelectedItem(resources.getString("qkey.msg.msg275"));
                                  SubscribeOKListener sok = new SubscribeOKListener();
                                  sok.actionPerformed(event);
                                  subscribeDialog = null;

                                  qBox.setSelectedItem(target_topic_name + TOPIC_SUFFIX);

                                  initTreePane();

                                  refreshMsgTableWithDestName();
                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }


    class CreateQueueForTreeListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            final JTextField queue_name_input = new JTextField();
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resources.getString("qkey.msg.msg344"));
            panel.add(BorderLayout.NORTH, lbl);
            panel.add(BorderLayout.CENTER, queue_name_input);

            popupConfirmationDialog(resources.getString("qkey.msg.msg345"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String target_queue_name = queue_name_input.getText();
                            if (target_queue_name != null && target_queue_name.length() != 0) {
                                try {

                                    bf.getBrokerAdmin().addQueue(target_queue_name);
                                    //popupMessageDialog("cmd", ta, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"));
                                    destinationNamesForDisplayQueue = new ArrayList();
                                    destinationNamesForDisplayTopic = new ArrayList();
                                    collectDestination();
                                    qBox.setSelectedItem(target_queue_name + QUEUE_SUFFIX);
                                    refreshMsgTableWithDestName();
                                    initTreePane();
                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });

        }
    }



    class CreateQueueListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            final JTextField queue_name_input = new JTextField();
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resourcesamq.getString("qkey.amq.msg.msg046"));
            panel.add(BorderLayout.NORTH, lbl);
            panel.add(BorderLayout.CENTER, queue_name_input);

            popupConfirmationDialog(resourcesamq.getString("qkey.amq.msg.msg047"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String target_queue_name = queue_name_input.getText();
                            if (target_queue_name != null && target_queue_name.length() != 0) {
                                try {

                                  bf.getBrokerAdmin().addQueue(target_queue_name);
                                  destinationNamesForDisplayQueue = new ArrayList();
                                  destinationNamesForDisplayTopic = new ArrayList();
                                  collectDestination();
                                  initTreePane();
                                  qBox.setSelectedItem(target_queue_name + QUEUE_SUFFIX);
                                  refreshMsgTableWithDestName();
                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }


    class CreateTopicListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            final JTextField topic_name_input = new JTextField();
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resourcesamq.getString("qkey.amq.msg.msg051"));
            panel.add(BorderLayout.NORTH, lbl);
            panel.add(BorderLayout.CENTER, topic_name_input);

            popupConfirmationDialog(resourcesamq.getString("qkey.amq.msg.msg049"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String target_topic_name = topic_name_input.getText();
                            if (target_topic_name != null && target_topic_name.length() != 0) {
                                try {
                                  bf.getBrokerAdmin().addTopic(target_topic_name);

                                  //DUMMY
                                  matesakiBox3 = new JComboBox();
                                  importTopicNamesToMATESAKIBOX3();
                                  matesakiBox3.setSelectedItem(target_topic_name);
                                  localstoreBox = new JComboBox();
                                  subscribeTemplateBox = new JComboBox();
                                  subscribeDialog = new JDialog();
                                  importLocalStoreNamesToLOCALSTOREBOX();
                                  localstoreBox.setSelectedItem(resources.getString("qkey.msg.msg275"));
                                  SubscribeOKListener sok = new SubscribeOKListener();
                                  sok.actionPerformed(event);
                                  subscribeDialog = null;
                                  destinationNamesForDisplayQueue = new ArrayList();
                                  destinationNamesForDisplayTopic = new ArrayList();
                                  collectDestination();
                                  initTreePane();
                                  qBox.setSelectedItem(target_topic_name + TOPIC_SUFFIX);

                                  refreshMsgTableWithDestName();
                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }

    class DeleteQueueForTreeListener implements ActionListener {


        public void actionPerformed(ActionEvent event) {

            final TreeIconPanel.DestInfo di = treePane.getSelectedDestInfo();
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resources.getString("qkey.msg.msg351") + di.destinationName + resources.getString("qkey.msg.msg352"));
            panel.add(BorderLayout.NORTH, lbl);

            popupConfirmationDialog(resources.getString("qkey.msg.msg350"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "1.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String selectedQueue = (String)di.destinationName;
                            if (selectedQueue != null && selectedQueue.length() != 0) {
                                try {
                                  DestinationFacade df = new DestinationFacade(bf);
                                  df.setJMSDestination(selectedQueue);
                                  df.setJMSDestinationType("queue");
                                  df.removeDestination();

                                  String dest_with_suffix = selectedQueue + QUEUE_SUFFIX;
                                  removeNamedTabbedPane(dest_with_suffix);
                                  int selidx = tabbedPane.getSelectedIndex();
                                  if (selidx != -1) {
                                    refreshTableOnCurrentSelectedTab();
                                     String tabname = tabbedPane.getTitleAt(selidx);
                                     qBox.setSelectedItem(tabname);
                                  }
                                  removeDestRelatedCache(dest_with_suffix);
                                  destinationNamesForDisplayQueue = new ArrayList();
                                  destinationNamesForDisplayTopic = new ArrayList();
                                  collectDestination();
                                  initTreePane();

                                  refreshMsgTableWithDestName();

                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }


    class DeleteTopicForTreeListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            final TreeIconPanel.DestInfo di = treePane.getSelectedDestInfo();
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(resources.getString("qkey.msg.msg354") + di.destinationName + resources.getString("qkey.msg.msg355"));
            panel.add(BorderLayout.NORTH, lbl);

            popupConfirmationDialog(resources.getString("qkey.msg.msg353"), panel, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Flagbase + "6.png"),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;
                            String selectedTopic = (String)di.destinationName;
                            if (selectedTopic != null && selectedTopic.length() != 0) {
                                try {
                                  DestinationFacade df = new DestinationFacade(bf);
                                  df.setJMSDestination(selectedTopic);
                                  df.setJMSDestinationType("topic");
                                  df.removeDestination();

                                  String dest_with_suffix = selectedTopic + TOPIC_SUFFIX;
                                  removeNamedTabbedPane(dest_with_suffix);

                                  int selidx = tabbedPane.getSelectedIndex();
                                  if (selidx != -1) {
                                    refreshTableOnCurrentSelectedTab();
                                     String tabname = tabbedPane.getTitleAt(selidx);
                                     qBox.setSelectedItem(tabname);
                                  }
                                  removeDestRelatedCache(dest_with_suffix);
                                  stopSubscriberThread(dest_with_suffix);
                                  subscribe_thread_status.remove(dest_with_suffix);

                                  //関連するローカルストアからこのトピック名を削除
                                  ArrayList local_copy_to = lsm.getCopyToListOfTheDestination(dest_with_suffix);
                                  for (int i = 0 ; i < local_copy_to.size(); i++) {
                                      String local_name_without_suffix = (String)local_copy_to.get(i);
                                      LocalStoreProperty lsp = lsm.getLocalStoreProperty(local_name_without_suffix);
                                      lsp.removeFromDests(dest_with_suffix);
                                      lsm.updateAndSaveLocalStoreProperty(lsp);
                                  }
                                  lsm.removeRelatedEntryOfSubscribeDest(dest_with_suffix);

                                  destinationNamesForDisplayQueue = new ArrayList();
                                  destinationNamesForDisplayTopic = new ArrayList();
                                  collectDestination();
                                  initTreePane();

                                  refreshMsgTableWithDestName();

                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }

                            }

                        }
                    });



        }
    }


    class ShutdownBrokerListener implements ActionListener {


        public void actionPerformed(ActionEvent event) {

           JTextArea jta = new JTextArea("", 3, 35);
           String vtitle = null;
           String msg = resources.getString("qkey.msg.msg365");
           try {
             msg += bf.getBrokerAdmin().getBrokerName();
           } catch (Exception bfe) {
               //nop
           }

           msg += resourcesamq.getString("qkey.amq.msg.msg056");
           vtitle = resourcesamq.getString("qkey.amq.msg.msg058");
           
           jta.setText(msg);
           jta.setEditable(false);
           jta.setBackground(Color.WHITE);




            popupConfirmationDialog(vtitle, jta , QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Disconnect),
                    new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            confirmDialog.dispose();
                            confirmDialog = null;

                                try {


                                   bf.getBrokerAdmin().stop();
                                    TextArea ta = new TextArea("", 2, 40, TextArea.SCROLLBARS_NONE);
                                    ta.setEditable(false);
                                    ta.setBackground(Color.WHITE);

                                    ta.setText(resourcesamq.getString("qkey.amq.msg.msg060"));
                                    popupMessageDialog(resourcesamq.getString("qkey.amq.msg.msg061"), ta, QBrowserIconsFactory.getImageIcon(QBrowserIconsFactory.Shutdown));
                                    moveToDisconnectStatus();
                                    

                                } catch (Exception e) {
                                    popupErrorMessageDialog(e);
                                }



                        }
                    });

        }
    }






}
