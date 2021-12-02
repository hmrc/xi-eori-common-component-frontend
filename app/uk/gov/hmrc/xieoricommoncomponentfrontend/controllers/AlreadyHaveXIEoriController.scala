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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{already_have_xi_eori, error_template}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AlreadyHaveXIEoriController @Inject() (
  authAction: AuthAction,
  sessionCache: SessionCache,
  xiEoriExistsView: already_have_xi_eori,
  errorTemplateView: error_template,
  mcc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def xiEoriAlreadyExists: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        sessionCache.subscriptionDisplay map {
          case Some(response) =>
            populateView(response)
          case None => InternalServerError(errorTemplateView())
        }
    }

  def populateView(
    subscriptionDisplayDetails: SubscriptionDisplayResponseDetail
  )(implicit hc: HeaderCarrier, request: Request[AnyContent]): Result =
    subscriptionDisplayDetails.XI_Subscription match {
      case Some(resp) => Ok(xiEoriExistsView(resp.XI_EORINo))
      case None       => InternalServerError(errorTemplateView())
    }

}
