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
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, EnrolmentExtractor, GroupEnrolmentExtractor}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{ExistingEori, LoggedInUserWithEnrolments}
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.HaveEUEoriFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.HaveEUEori
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.have_eu_eori

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HaveEUEoriController @Inject() (
  authAction: AuthAction,
  haveEUEoriView: have_eu_eori,
  formProvider: HaveEUEoriFormProvider,
  groupEnrolment: GroupEnrolmentExtractor,
  mcc: MessagesControllerComponents
)(implicit val ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with EnrolmentExtractor {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(haveEUEoriView(form)))
    }

  def submit: Action[AnyContent] = authAction.ggAuthorisedUserWithEnrolmentsAction {
    implicit request => loggedInUser: LoggedInUserWithEnrolments =>
    form
      .bindFromRequest()
      .fold(formWithErrors => Future.successful(BadRequest(haveEUEoriView(formWithErrors))),
        value => value match {
          case HaveEUEori.Yes =>
            Future.successful(Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded()))
          case HaveEUEori.No =>
            existingEori(loggedInUser).map(destinationsByExistingEori(_))
          })

  }

  private def existingEori(
                            user: LoggedInUserWithEnrolments
                          )(implicit headerCarrier: HeaderCarrier): Future[Option[ExistingEori]] =
    groupEnrolment.groupIdEnrolments(user.groupId.getOrElse(throw MissingGroupId())).map {
      groupEnrolments =>
        existingEoriForUserOrGroup(user.enrolments.enrolments, groupEnrolments)
    }

  private def destinationsByExistingEori(existingEori: Option[ExistingEori]): Result = existingEori match {
    case Some(_) =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.onPageLoad())
    case None =>
      Redirect(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.YouAlreadyHaveEoriController.eoriAlreadyExists()
      )
  }
}

case class MissingGroupId() extends Exception(s"User doesn't have groupId")
