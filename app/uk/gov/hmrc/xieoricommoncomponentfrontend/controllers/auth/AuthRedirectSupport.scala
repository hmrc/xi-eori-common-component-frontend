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

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth

import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.{AuthorisationException, InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

trait AuthRedirectSupport extends AuthRedirects {

  override val config: Configuration
  override val env: Environment

  private def continueUrl(implicit request: Request[AnyContent]) =
    config.get[String]("external-urls.loginContinue")

  def withAuthRecovery(implicit request: Request[AnyContent]): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession => toGGLogin(continueUrl = continueUrl)
    case _: InsufficientEnrolments =>
      Redirect(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.YouCannotUseServiceController.unauthorisedPage()
      )
    case _: AuthorisationException =>
      Redirect(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.YouCannotUseServiceController.unauthorisedPage()
      )
  }

}
