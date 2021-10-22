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
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ConfirmDetailsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.util.EoriUtils
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.confirm_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmDetailsController @Inject() (
  authAction: AuthAction,
  confirmDetailsView: confirm_details,
  formProvider: ConfirmDetailsFormProvider,
  mcc: MessagesControllerComponents,
  connector: SubscriptionDisplayConnector,
  utils: EoriUtils
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val queryParameters =
    List("EORI" -> "GB123456789012", "regime" -> "CDS", "acknowledgementReference" -> utils.generateUUIDAsString)

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => loggedInUser: LoggedInUserWithEnrolments =>
        for {
          subscriptionDisplay <- connector.call(queryParameters)
        } yield (Ok(
          confirmDetailsView(form, ConfirmDetailsViewModel(subscriptionDisplay, loggedInUser.affinityGroup.get))
        ))

    }

  def submit(): Action[AnyContent] = authAction.ggAuthorisedUserWithEnrolmentsAction {
    implicit request => loggedInUser: LoggedInUserWithEnrolments =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            for {
              subscriptionDisplay <- connector.call(queryParameters)
            } yield (BadRequest(
              confirmDetailsView(
                formWithErrors,
                ConfirmDetailsViewModel(subscriptionDisplay, loggedInUser.affinityGroup.get)
              )
            )),
          value => destinationsByAnswer(value)
        )
  }

  private def destinationsByAnswer(confirmDetails: ConfirmDetails): Future[Result] = confirmDetails match {
    case ConfirmDetails.confirmedDetails =>
      Future.successful(
        Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad())
      )
    case ConfirmDetails.changeCredentials =>
      Future.successful(
        Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded())
      )
    case ConfirmDetails.changeDetails =>
      Future.successful(
        Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded())
      )
  }

}
