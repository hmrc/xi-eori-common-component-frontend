/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.AppConfig
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.display_sign_out

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LogoutController @Inject() (
  authAction: AuthAction,
  appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  sessionCache: SessionCache,
  displaySignOutView: display_sign_out
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) {

  def logout: Action[AnyContent] = authAction.ggAuthorisedUserWithEnrolmentsAction {
    implicit request => _: LoggedInUserWithEnrolments =>
      sessionCache.remove.map(_ => Redirect(appConfig.signOutUrl))
  }

  def timeout: Action[AnyContent] = authAction.ggAuthorisedUserWithEnrolmentsAction {
    implicit request => _: LoggedInUserWithEnrolments =>
      sessionCache.remove.map(
        _ =>
          Redirect(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage
          ).withNewSession
      )
  }

  def displayTimeOutPage(): Action[AnyContent] = Action { implicit request =>
    Ok(displaySignOutView())
  }

}
