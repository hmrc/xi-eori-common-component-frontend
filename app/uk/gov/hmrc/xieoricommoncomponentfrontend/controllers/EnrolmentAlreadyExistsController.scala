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
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{registration_exists, registration_exists_group}

import javax.inject.Inject
import scala.concurrent.Future

class EnrolmentAlreadyExistsController @Inject() (
  authAction: AuthAction,
  registrationExistsView: registration_exists,
  registrationExistsForGroupView: registration_exists_group,
  mcc: MessagesControllerComponents
) extends FrontendController(mcc) with I18nSupport {

  // Note: permitted for user with service enrolment
  def enrolmentAlreadyExists: Action[AnyContent] =
    authAction.ggAuthorisedUserWithServiceAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(registrationExistsView()))
    }

  // Note: permitted for user with service enrolment
  def enrolmentAlreadyExistsForGroup: Action[AnyContent] =
    authAction.ggAuthorisedUserWithServiceAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(registrationExistsForGroupView()))
    }

}
