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
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.UserAnswersCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.PBEConfirmAddressFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.PBEConfirmAddress
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{error_template, pbe_confirm_address}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PBEConfirmAddressController @Inject() (
  authAction: AuthAction,
  mcc: MessagesControllerComponents,
  userAnswersCache: UserAnswersCache,
  formProvider: PBEConfirmAddressFormProvider,
  pbeConfirmAddressView: pbe_confirm_address,
  errorTemplateView: error_template
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {
  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        userAnswersCache.getAddressDetails().flatMap {
          case Some(address) =>
            userAnswersCache.getConfirmAddress().map {
              case Some(confirmAddress) =>
                Ok(pbeConfirmAddressView(address, form.fill(PBEConfirmAddress.mapValues(confirmAddress))))
              case None => Ok(pbeConfirmAddressView(address, form))
            }
          case None => Future.successful(InternalServerError(errorTemplateView()))
        }
    }

  def submit: Action[AnyContent] = authAction.ggAuthorisedUserWithEnrolmentsAction {
    implicit request => _: LoggedInUserWithEnrolments =>
      userAnswersCache.getAddressDetails().flatMap {
        case Some(address) =>
          form.bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(pbeConfirmAddressView(address, formWithErrors))),
              value => userAnswersCache.cacheConfirmAddress(value).map(_ => destinationsByAnswer(value))
            )
        case None => Future.successful(InternalServerError(errorTemplateView()))
      }

  }

  private def destinationsByAnswer(confirmDetails: PBEConfirmAddress): Result = confirmDetails match {
    case PBEConfirmAddress.confirmedAddress =>
      Redirect(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.onPageLoad()
      )
    case PBEConfirmAddress.changeAddress =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEAddressLookupController.onPageLoad())
    case PBEConfirmAddress.enterManually =>
      Redirect(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.onPageLoad()
      )
  }

}
