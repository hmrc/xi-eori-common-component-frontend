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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, GroupEnrolmentExtractor}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.XiSubscription
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.RegistrationDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.error_template

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject() (
  authAction: AuthAction,
  groupEnrolment: GroupEnrolmentExtractor,
  subscriptionDisplayService: SubscriptionDisplayService,
  errorTemplateView: error_template,
  sessionCache: SessionCache,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => loggedInUser: LoggedInUserWithEnrolments =>
        groupEnrolment.getEori(loggedInUser).flatMap {
          case Some(gbEori) =>
            subscriptionDisplayService.getSubscriptionDisplay(gbEori).flatMap {
              case Right(response) =>
                destinationsByAnswer(response.XI_Subscription)
              case Left(_) => Future.successful(InternalServerError(errorTemplateView()))
            }
          case None =>
            sessionCache.saveRegistrationDetails(RegistrationDetails()).map(
              _ =>
                Redirect(
                  uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.onPageLoad()
                )
            )
        }

    }

  private def destinationsByAnswer(xiSubscription: Option[XiSubscription])(implicit hc: HeaderCarrier): Future[Result] =
    xiSubscription match {
      case Some(_) =>
        sessionCache.saveRegistrationDetails(RegistrationDetails()).map(
          _ =>
            Redirect(
              uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AlreadyHaveXIEoriController.xiEoriAlreadyExists()
            )
        )

      case None =>
        Future.successful(
          Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.onPageLoad())
        )
    }

}
