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

import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, AuthRedirectSupport}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail.ContactInformation
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmContactDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.confirm_contact_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmContactDetailsController @Inject() (
  override val config: Configuration,
  override val env: Environment,
  authAction: AuthAction,
  confirmContactDetailsView: confirm_contact_details,
  mcc: MessagesControllerComponents,
  sessionCache: SessionCache
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with AuthRedirectSupport {

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        sessionCache.subscriptionDisplay.flatMap {
          case Some(subscriptionDisplay) =>
            subscriptionDisplay.contactInformation.map(populateView(_))
              .getOrElse(Future.successful(Redirect("/")))
          case None =>
            Future.successful(
              Redirect(
                uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage()
              ).withNewSession
            )
        }
    }

  def populateView(
    contactInformation: ContactInformation
  )(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] =
    Future.successful(Ok(confirmContactDetailsView(ConfirmContactDetailsViewModel(contactInformation))))

}
