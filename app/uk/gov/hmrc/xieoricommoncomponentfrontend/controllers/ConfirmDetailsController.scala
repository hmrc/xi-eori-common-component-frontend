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
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.{ConfirmDetailsFormProvider, TradeWithNIFormProvider}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{ConfirmDetails, TradeWithNI}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.TradeWithNI.{No, Yes}
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{confirm_details, trade_with_ni}

import javax.inject.Inject
import scala.concurrent.Future

class ConfirmDetailsController @Inject()(
  authAction: AuthAction,
  confirmDetailsView: confirm_details,
  formProvider: ConfirmDetailsFormProvider,
  mcc: MessagesControllerComponents
) extends FrontendController(mcc) with I18nSupport {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(confirmDetailsView(form)))
    }

  def submit: Action[AnyContent] = Action { implicit request =>
    form
      .bindFromRequest()
      .fold(formWithErrors => BadRequest(confirmDetailsView(formWithErrors)), value => destinationsByAnswer(value))
  }

  private def destinationsByAnswer(confirmDetails: ConfirmDetails): Result = confirmDetails match {
    case ConfirmDetails.confirmedDetails =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad())
    case ConfirmDetails.changeCredentials =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded())
    case ConfirmDetails.changeDetails =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded())
  }

}
