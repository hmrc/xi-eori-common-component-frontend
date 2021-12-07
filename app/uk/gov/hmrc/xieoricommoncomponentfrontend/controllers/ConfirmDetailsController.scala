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
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.AppConfig
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, AuthRedirectSupport}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ConfirmDetailsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{confirm_details, error_template}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmDetailsController @Inject() (
  override val config: Configuration,
  override val env: Environment,
  authAction: AuthAction,
  appConfig: AppConfig,
  confirmDetailsView: confirm_details,
  formProvider: ConfirmDetailsFormProvider,
  mcc: MessagesControllerComponents,
  userAnswersCache: UserAnswersCache,
  sessionCache: SessionCache
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with AuthRedirectSupport {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => loggedInUser: LoggedInUserWithEnrolments =>
        sessionCache.subscriptionDisplay.flatMap {
          case Some(response) =>
            populateView(response, loggedInUser.userAffinity())
          case None =>
            Future.successful(
              Redirect(
                uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage()
              ).withNewSession
            )
        }

    }

  def populateView(subscriptionDisplayDetails: SubscriptionDisplayResponseDetail, userAffinity: AffinityGroup)(implicit
    hc: HeaderCarrier,
    request: Request[AnyContent]
  ): Future[Result] =
    userAnswersCache.getConfirmDetails().map {
      case Some(confirmDetails) =>
        Ok(
          confirmDetailsView(
            form.fill(ConfirmDetails.mapValues(confirmDetails)),
            ConfirmDetailsViewModel(subscriptionDisplayDetails, userAffinity),
            getXIVatNumber(subscriptionDisplayDetails)
          )
        )
      case None =>
        Ok(
          confirmDetailsView(
            form,
            ConfirmDetailsViewModel(subscriptionDisplayDetails, userAffinity),
            getXIVatNumber(subscriptionDisplayDetails)
          )
        )
    }

  def submit(): Action[AnyContent] = authAction.ggAuthorisedUserWithEnrolmentsAction {
    implicit request => loggedInUser: LoggedInUserWithEnrolments =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            sessionCache.subscriptionDisplay.map {
              case Some(response) =>
                BadRequest(
                  confirmDetailsView(
                    formWithErrors,
                    ConfirmDetailsViewModel(response, loggedInUser.userAffinity()),
                    getXIVatNumber(response)
                  )
                )
              case None =>
                Redirect(
                  uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage()
                ).withNewSession
            },
          value => userAnswersCache.cacheConfirmDetails(value).map(_ => destinationsByAnswer(value))
        )
  }

  private def destinationsByAnswer(confirmDetails: ConfirmDetails): Result = confirmDetails match {
    case ConfirmDetails.confirmedDetails =>
      Redirect(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.onPageLoad()
      )
    case ConfirmDetails.changeCredentials =>
      toGGLogin(appConfig.loginContinueUrl).withNewSession
    case ConfirmDetails.changeDetails =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ChangeDetailsController.incorrectDetails())
  }

  private def getXIVatNumber(subscriptionDisplayDetails: SubscriptionDisplayResponseDetail) =
    for {
      xiSubscription <- subscriptionDisplayDetails.XI_Subscription
      xiVatNumber    <- xiSubscription.XI_VATNumber
    } yield xiVatNumber

}
