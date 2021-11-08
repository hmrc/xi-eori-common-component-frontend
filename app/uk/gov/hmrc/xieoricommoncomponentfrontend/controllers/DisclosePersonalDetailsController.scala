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
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.DisclosePersonalDetailsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.disclose_personal_details

import javax.inject.Inject
import scala.concurrent.Future

class DisclosePersonalDetailsController @Inject() (
  authAction: AuthAction,
  disclosePersonalDetailsView: disclose_personal_details,
  formProvider: DisclosePersonalDetailsFormProvider,
  mcc: MessagesControllerComponents
) extends FrontendController(mcc) with I18nSupport {

  private val form = formProvider()

  // Note: permitted for user with service enrolment
  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(disclosePersonalDetailsView(form)))
    }

  def submit: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => loggedInUser: LoggedInUserWithEnrolments =>
      form.bindFromRequest.fold(
        invalidForm => Future.successful(BadRequest(disclosePersonalDetailsView(invalidForm))),
        value =>
          if (loggedInUser.affinityGroup.get equals AffinityGroup.Organisation)
            Future.successful(
              Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.onPageLoad())
            )
          else
            Future.successful(
              Redirect(
                uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded()
              )
            )
      )
    }

}
