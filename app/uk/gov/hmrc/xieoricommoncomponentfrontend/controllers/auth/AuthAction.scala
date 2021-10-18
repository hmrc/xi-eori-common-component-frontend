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

import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, internalId, email => ggEmail, _}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
trait IdentifierAction extends AuthRedirectSupport with AuthorisedFunctions with AccessController

class AuthAction @Inject() (
  override val config: Configuration,
  override val env: Environment,
  override val authConnector: AuthConnector,
  action: DefaultActionBuilder
)(implicit ec: ExecutionContext)
    extends IdentifierAction {

  private type RequestProcessorSimple =
    Request[AnyContent] => LoggedInUserWithEnrolments => Future[Result]

  private type RequestProcessorExtended =
    Request[AnyContent] => Option[String] => LoggedInUserWithEnrolments => Future[Result]

  private val baseRetrievals     = ggEmail and credentialRole and affinityGroup
  private val extendedRetrievals = baseRetrievals and internalId and allEnrolments and groupIdentifier

  /**
    * Allows Gov Gateway user with correct user type, affinity group and no enrolment to service
    */
  def ggAuthorisedUserWithEnrolmentsAction(requestProcessor: RequestProcessorSimple) =
    action.async { implicit request =>
      authorise(requestProcessor)
    }

  private def authorise(requestProcessor: RequestProcessorSimple)(implicit request: Request[AnyContent]) = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthProviders(GovernmentGateway))
      .retrieve(extendedRetrievals) {
        case currentUserEmail ~ userCredentialRole ~ userAffinityGroup ~ userInternalId ~ userAllEnrolments ~ groupId =>
          transformRequest(
            Right(requestProcessor),
            LoggedInUserWithEnrolments(userAffinityGroup, userInternalId, userAllEnrolments, currentUserEmail, groupId),
            userCredentialRole
          )
      } recover withAuthRecovery(request)
  }

  private def transformRequest(
    requestProcessor: Either[RequestProcessorExtended, RequestProcessorSimple],
    loggedInUser: LoggedInUserWithEnrolments,
    userCredentialRole: Option[CredentialRole]
  )(implicit request: Request[AnyContent], hc: HeaderCarrier) = {

    def action: Future[Result] =
      requestProcessor fold (_(request)(loggedInUser.internalId)(loggedInUser), _(request)(loggedInUser))
    permitUserOrRedirect(loggedInUser.affinityGroup, userCredentialRole)(action)
  }

}
