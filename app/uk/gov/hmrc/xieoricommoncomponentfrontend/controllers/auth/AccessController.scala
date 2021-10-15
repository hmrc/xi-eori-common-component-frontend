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
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.{AffinityGroup, CredentialRole, Enrolment, User}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.EnrolmentResponse
import scala.concurrent.{ExecutionContext, Future}

trait AccessController extends EnrolmentExtractor {

  def permitUserOrRedirect(affinityGroup: Option[AffinityGroup], credentialRole: Option[CredentialRole])(
    action: Future[Result]
  )(implicit request: Request[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    def isPermittedUserType: Boolean =
      affinityGroup match {
        case Some(Agent)        => false
        case Some(Organisation) => credentialRole.fold(false)(cr => cr == User)
        case _                  => true
      }

    if (!isPermittedUserType)
      Future.successful(Redirect(routes.YouCannotUseServiceController.page))
    else
      action
  }

}

case class MissingGroupId() extends Exception(s"User doesn't have groupId")
