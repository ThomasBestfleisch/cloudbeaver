/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2020 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudbeaver.model;

import io.cloudbeaver.WebServiceUtils;
import io.cloudbeaver.model.session.WebSession;
import io.cloudbeaver.server.CBConstants;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.connection.DBPAuthModelDescriptor;
import org.jkiss.dbeaver.model.impl.auth.AuthModelDatabaseNative;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.navigator.DBNBrowseSettings;
import org.jkiss.dbeaver.model.preferences.DBPPropertySource;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Web connection info
 */
public class WebConnectionInfo {

    private final WebSession session;
    private DBPDataSourceContainer dataSourceContainer;
    private WebServerError connectError;

    private String connectTime;
    private String serverVersion;
    private String clientVersion;

    public WebConnectionInfo(WebSession session, DBPDataSourceContainer ds) {
        this.session = session;
        this.dataSourceContainer = ds;
    }

    public WebSession getSession() {
        return session;
    }

    public DBPDataSourceContainer getDataSourceContainer() {
        return dataSourceContainer;
    }

    public DBPDataSource getDataSource() {
        return dataSourceContainer.getDataSource();
    }

    @Property
    public String getId() {
        return dataSourceContainer.getId();
    }

    @Property
    public String getDriverId() {
        return WebServiceUtils.makeDriverFullId(dataSourceContainer.getDriver());
    }

    @Property
    public String getName() {
        return dataSourceContainer.getName();
    }

    @Property
    public String getDescription() {
        return dataSourceContainer.getDescription();
    }

    @Property
    public String getProperties() {
        return null;
    }

    @Property
    public boolean isConnected() {
        return dataSourceContainer.isConnected();
    }

    @Property
    public boolean isTemplate() {
        return dataSourceContainer.isTemplate();
    }

    @Property
    public boolean isProvided() {
        return dataSourceContainer.isProvided();
    }

    @Property
    public boolean isReadOnly() {
        return dataSourceContainer.isConnectionReadOnly();
    }

    @Property
    public String getConnectTime() {
        return CBConstants.ISO_DATE_FORMAT.format(dataSourceContainer.getConnectTime());
    }

    @Property
    public WebServerError getConnectError() {
        return connectError;
    }

    public void setConnectError(Throwable connectError) {
        this.connectError = connectError == null ? null : new WebServerError(connectError);
    }

    public void setConnectTime(String connectTime) {
        this.connectTime = connectTime;
    }

    @Property
    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Property
    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Property
    public String[] getFeatures() {
        List<String> features = new ArrayList<>();

        if (dataSourceContainer.isConnected()) {
            features.add("connected");
        }
        if (dataSourceContainer.isHidden()) {
            features.add("virtual");
        }
        if (dataSourceContainer.isTemporary()) {
            features.add("temporary");
        }
        if (dataSourceContainer.isConnectionReadOnly()) {
            features.add("readOnly");
        }
        if (dataSourceContainer.isProvided()) {
            features.add("provided");
        }

        return features.toArray(new String[0]);
    }

    @Property
    public DBNBrowseSettings getNavigatorSettings() {
        return dataSourceContainer.getNavigatorSettings();
    }

    @Property
    public boolean isAuthNeeded() {
        return !dataSourceContainer.isConnected() &&
            !dataSourceContainer.isSavePassword() &&
            !dataSourceContainer.getDriver().isAnonymousAccess();
    }

    @Property
    public String getAuthModel() {
        return dataSourceContainer.getConnectionConfiguration().getAuthModelId();
    }

    @Property
    public WebPropertyInfo[] getAuthProperties() {
        String authModelId = dataSourceContainer.getConnectionConfiguration().getAuthModelId();
        if (CommonUtils.isEmpty(authModelId)) {
            authModelId = AuthModelDatabaseNative.ID;
        }
        DBPAuthModelDescriptor authModel = DBWorkbench.getPlatform().getDataSourceProviderRegistry().getAuthModel(authModelId);
        if (authModel == null) {
            return new WebPropertyInfo[0];
        }

        DBPPropertySource credentialsSource = authModel.createCredentialsSource(dataSourceContainer);
        return Arrays.stream(credentialsSource.getProperties())
            .map(p -> new WebPropertyInfo(session, p, credentialsSource)).toArray(WebPropertyInfo[]::new);
    }

}
