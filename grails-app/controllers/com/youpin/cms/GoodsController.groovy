
package com.youpin.cms

import org.springframework.dao.DataIntegrityViolationException

import com.youpin.cms.util.GoodsFinder

class GoodsController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

//	def beforeInterceptor = [action: this.&auth, except: 'login']
//	// defined with private scope, so it's not considered an action
//	private auth() {
//		if (!session.user) {
//			redirect(controller:'login', action: 'userLogin')
//			return false
//		}
//	}
	
	def index() {
		redirect(action: "list", params: params)
	}

	def list(Integer max) {
		params.goodsStatus = params.goodsStatus?:'UNUSED'
		GoodsService service = new GoodsService()
		service.params =  params
		service.max = max
		render(view: "list", model: service.service())
	}

	def create() {
		[goodsInstance: new Goods(params)]
	}

	def save() {
		def goodsInstance = new Goods(params)
		if (!goodsInstance.save(flush: true)) {
			render(view: "create", model: [goodsInstance: goodsInstance])
			return
		}

		flash.message = message(code: 'default.created.message', args: [
			message(code: 'goods.label', default: 'Goods'),
			goodsInstance.id
		])
		redirect(action: "show", id: goodsInstance.id)
	}

	def show(Long id) {
		def goodsInstance = Goods.get(id)
		if (!goodsInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'goods.label', default: 'Goods'),
				id
			])
			redirect(action: "list")
			return
		}

		[goodsInstance: goodsInstance]
	}

	def edit(Long id) {
		def goodsInstance = Goods.get(id)
		if (!goodsInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'goods.label', default: 'Goods'),
				id
			])
			redirect(action: "list")
			return
		}

		[goodsInstance: goodsInstance]
	}

	def update(Long id, Long version) {
		def goodsInstance = Goods.get(id)
		if (!goodsInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'goods.label', default: 'Goods'),
				id
			])
			redirect(action: "list")
			return
		}

		if (version != null) {
			if (goodsInstance.version > version) {
				goodsInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
						[
							message(code: 'goods.label', default: 'Goods')] as Object[],
						"Another user has updated this Goods while you were editing")
				render(view: "edit", model: [goodsInstance: goodsInstance])
				return
			}
		}
		params['originalPrice'] = Double.parseDouble(params['originalPriceDes'])
		params['promotionPrice'] = Double.parseDouble(params['promotionPriceDes'])
		params['updateDate'] = new Date()
		params['updateDateDes'] = params['updateDate'].format('yyyy-MM-dd')
		
		goodsInstance.properties = params

		if (!goodsInstance.save(flush: true)) {
			render(view: "edit", model: [goodsInstance: goodsInstance])
			return
		}

		flash.message = message(code: 'default.updated.message', args: [
			message(code: 'goods.label', default: 'Goods'),
			goodsInstance.id
		])
		redirect(action: "show", id: goodsInstance.id)
	}

	def delete(Long id) {
		log.info(params)
		def goodsInstance = Goods.get(id)
		if (!goodsInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'goods.label', default: 'Goods'),
				id
			])
			redirect(action: params['menu'])
			return
		}

		try {
			goodsInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [
				message(code: 'goods.label', default: 'Goods'),
				id
			])
			redirect(action: params['menu'])
		}
		catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [
				message(code: 'goods.label', default: 'Goods'),
				id
			])
			redirect(action: "show", id: id)
		}
	}

	def load(){
		new GoodsFinder().start()
		redirect(action:"list")
	}

	/**
	 * 修改状态，编辑，上架下架
	 * @param id
	 * @return
	 */
	def changeStatus(Long id){
		println params
		def goodsInstance = Goods.get(id)
		goodsInstance.properties = params
		goodsInstance.save(flush: true)
		if(goodsInstance.errors.errorCount !=0 ){
			return render(contentType: "text/json") {
				['success':false,'message':'不能这样操作']
			}
		}
		render(contentType: "text/json") { ['success':true] }
	}
	
	
}
