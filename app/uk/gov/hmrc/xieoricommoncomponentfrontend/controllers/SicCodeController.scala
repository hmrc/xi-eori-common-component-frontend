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
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{
  AuthAction,
  EnrolmentExtractor,
  GroupEnrolmentExtractor
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.SicCodeFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{error_template, sic_code}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SicCodeController @Inject() (
  authAction: AuthAction,
  sicCodeView: sic_code,
  formProvider: SicCodeFormProvider,
  mcc: MessagesControllerComponents,
  errorTemplateView: error_template,
  subscriptionDisplayService: SubscriptionDisplayService,
  groupEnrolment: GroupEnrolmentExtractor
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with EnrolmentExtractor {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(sicCodeView(form)))
    }

  def submit: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => loggedInUser: LoggedInUserWithEnrolments =>
      form.bindFromRequest.fold(
        invalidForm => Future.successful(BadRequest(sicCodeView(invalidForm))),
        value =>
          loggedInUser.affinityGroup match {
            case Some(AffinityGroup.Organisation) =>
              groupEnrolment.getEori(loggedInUser).flatMap {
                case Some(gbEori) =>
                  subscriptionDisplayService.getSubscriptionDisplay(gbEori).map {
                    case Right(response) =>
                      destinationsByNIPostCode(response.CDSEstablishmentAddress.postalCode)
                    case Left(_) => InternalServerError(errorTemplateView())
                  }
              }
            case _ =>
              Future.successful(
                Redirect(
                  uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded()
                )
              )

          }
      )
    }

  private def destinationsByNIPostCode(regPostcode: Option[String]): Result = regPostcode match {
    case Some(regPostcode) if !regPostcode.take(2).equalsIgnoreCase("BT") =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.onPageLoad())
    case _ =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded())
  }

}
