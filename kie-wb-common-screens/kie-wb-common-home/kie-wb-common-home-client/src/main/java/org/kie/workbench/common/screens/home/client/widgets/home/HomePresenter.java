/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.home.client.widgets.home;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.home.client.resources.i18n.HomeConstants;
import org.kie.workbench.common.screens.home.client.widgets.shortcut.ShortcutPresenter;
import org.kie.workbench.common.screens.home.model.HomeModel;
import org.kie.workbench.common.screens.home.model.HomeModelProvider;
import org.kie.workbench.common.screens.home.model.HomeShortcut;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.security.authz.AuthorizationManager;

@Dependent
@WorkbenchScreen(identifier = "org.kie.workbench.common.screens.home.client.HomePresenter")
public class HomePresenter {

    public interface View extends UberElement<HomePresenter> {

        void setWelcome(String welcome);

        void setDescription(String description);

        void setBackgroundImageUrl(String backgroundImageUrl);

        void addShortcut(ShortcutPresenter shortcutPresenter);
    }

    private View view;

    private TranslationService translationService;

    private HomeModelProvider modelProvider;

    private AuthorizationManager authorizationManager;

    private User user;

    private ManagedInstance<ShortcutPresenter> shortcutPresenters;

    @Inject
    public HomePresenter(final View view,
                         final TranslationService translationService,
                         final HomeModelProvider modelProvider,
                         final AuthorizationManager authorizationManager,
                         final User user,
                         final ManagedInstance<ShortcutPresenter> shortcutPresenters) {
        this.view = view;
        this.translationService = translationService;
        this.modelProvider = modelProvider;
        this.authorizationManager = authorizationManager;
        this.user = user;
        this.shortcutPresenters = shortcutPresenters;
    }

    public void setup() {
        final HomeModel model = modelProvider.get();

        view.setWelcome(model.getWelcome());
        view.setDescription(model.getDescription());
        view.setBackgroundImageUrl(model.getBackgroundImageUrl());
        model.getShortcuts().forEach(shortcut -> {
            if (authorize(shortcut)) {
                final ShortcutPresenter shortcutPresenter = shortcutPresenters.get();
                shortcutPresenter.setup(shortcut);
                view.addShortcut(shortcutPresenter);
            }
        });
    }

    private boolean authorize(final HomeShortcut shortcut) {
        if (shortcut.getResource() != null) {
            return authorizationManager.authorize(shortcut.getResource(),
                                                  shortcut.getResourceAction(),
                                                  user);
        } else if (shortcut.getPermission() != null) {
            return authorizationManager.authorize(shortcut.getPermission(),
                                                  user);
        } else {
            return true;
        }
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return translationService.format(HomeConstants.HomeName);
    }

    @WorkbenchPartView
    public UberElement<HomePresenter> getView() {
        return view;
    }
}
