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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ConfirmAddressFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmAddress
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{error_template, pbe_confirm_address}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PBEConfirmAddressController @Inject() (
  authAction: AuthAction,
  mcc: MessagesControllerComponents,
  userAnswersCache: UserAnswersCache,
  formProvider: ConfirmAddressFormProvider,
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
            userAnswersCache.getConfirmPBEAddress().map {
              case Some(confirmAddress) =>
                Ok(pbeConfirmAddressView(address, form.fill(ConfirmAddress.mapValues(confirmAddress))))
              case None => Ok(pbeConfirmAddressView(address, form))
            }
          case None =>
            Future.successful(
              Redirect(
                uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage()
              ).withNewSession
            )
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
        case None =>
          Future.successful(
            Redirect(
              uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage()
            ).withNewSession
          )
      }

  }

  private def destinationsByAnswer(confirmDetails: ConfirmAddress): Result = confirmDetails match {
    case ConfirmAddress.confirmedAddress =>
      Redirect(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmContactDetailsController.onPageLoad()
      )
    case ConfirmAddress.changeAddress =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEAddressLookupController.onPageLoad())
    case ConfirmAddress.enterManually =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.reviewPageLoad())
  }

}
