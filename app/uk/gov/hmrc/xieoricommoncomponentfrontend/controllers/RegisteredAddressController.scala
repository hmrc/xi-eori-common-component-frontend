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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.PBEAddressResultsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{AddressLookupFailure, AddressLookupSuccess}
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.registered_address

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegisteredAddressController @Inject()(
                                             authAction: AuthAction,
                                             mcc: MessagesControllerComponents,
                                             addressLookupConnector: AddressLookupConnector,
                                             sessionCache: SessionCache,
                                             registeredAddressView: registered_address
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {


  def onPageLoad(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request =>
        _: LoggedInUserWithEnrolments =>
          displayPage()
    }

  private def displayPage()(implicit request: Request[AnyContent]
  ): Future[Result] =
    sessionCache.addressLookupParams.flatMap {
      case Some(addressLookupParams) =>
        addressLookupConnector.lookup("BT274RL", Some("Civic Headquarters")).flatMap { response =>
          response match {
            case AddressLookupSuccess(addresses) if addresses.nonEmpty && addresses.forall(_.nonEmpty) =>
              Future.successful(
                Ok(
                  registeredAddressView(
                    PBEAddressResultsFormProvider.form(addresses.map(_.dropDownView)),
                    addressLookupParams,
                    addresses
                  )
                )
              )

            case AddressLookupFailure => throw AddressLookupException
          }
        }.recoverWith {
          case _: AddressLookupException.type => Future.successful(Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad()))
        }
      case _ => Future.successful(Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEAddressLookupController.onPageLoad()))
    }

}
case object AddressLookupException extends Exception("Address Lookup service is not available")

