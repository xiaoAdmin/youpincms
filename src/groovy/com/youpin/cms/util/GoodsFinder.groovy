package com.youpin.cms.util

import com.taobao.api.domain.TaobaokeItem
import com.youpin.cms.Goods


class GoodsFinder {
	/**
	 * 开始
	 */
	void start() {
		/*
		 * 1.查询商品
		 * 2.找出新商品,存入数据库
		 * 3.旧商品更新数据
		 */
		//TODO:添加商品分类参数
		['米折网专享'].each {keyword->
			[0L].each {cid->
				(1..5).each {pageNo->
					processGoods(keyword,cid,pageNo, 100)
				}
			}
		}
	}

	def processGoods(String keyword,long cid,long pageNo,long pageSize){
		def remoteGoods = findGoods(keyword,cid,pageNo, pageSize)
		def localGoodsMap = loadLocalGoods()
		remoteGoods.each {
			if (localGoodsMap.keySet().contains(it.numId)) {
				Goods goods = localGoodsMap.get(it.numId)
				[
					'originalPrice',
					'originalPriceDes',
					'promotionPrice',
					'promotionPriceDes',
					'updateDate',
					'updateDateDes'
				].each {property->
					goods[property] = it[property]
				}
				goods.save(flush: true)
			}else{
				it.cid = cid
				it.save()
			}
		}
		return remoteGoods.size()
	}

	def Map loadLocalGoods(){
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -7);
		def timestamp = c.getTime();

		def query = Goods.where { (creationDate > timestamp) }
		def map=[:]
		query.list()?.each{
			map[it.numId]=it
		}
		map
	}

	/**
	 * 获取goods列表
	 * @return
	 */
	def List<Goods> findGoods(String keyword,long cid,long pageNo,long pageSize){
		def tao = new TaobaoApi()
		def taokeList = tao.searchTaoke(keyword,cid, pageNo, pageSize, "desc")
		taokeList.collect { createGoods(it) }
	}

	/**
	 * 根据taokeitem创建goods
	 * @param item
	 * @return
	 */
	def Goods createGoods(TaobaokeItem item){
		def goods = new Goods([
			numId:item.numIid,
			nick:item.nick,
			goodsName:item.title,
			originalPrice:Double.parseDouble(item.price),
			originalPriceDes:item.price,
			goodsLocation:item.itemLocation,
			sellerCreditScore:item.sellerCreditScore,
			clickUrl:item.clickUrl,
			shopClickUrl:item.shopClickUrl,
			picUrl:item.picUrl,
			commissionRate:item.commissionRate,
			commission:item.commission,
			commissionNum:item.commissionNum,
			commissionVolume:item.commissionVolume,
			volume:item.volume,
			promotionPrice:Double.parseDouble(item.promotionPrice),
			promotionPriceDes:item.promotionPrice
		])
		//TODO:找个好点的正则
		goods['goodsName'] = goods.getGoodsName().
				replaceAll("^((【)?米折网专享(】)?)|(<[a-zA-Z]+[1-9]?[^><]*>)|(</[a-zA-Z]+[1-9]?>)", "")
		return goods
	}

	def show(TaobaokeItem item){
		println "淘宝客商品数字id:"+ item.getNumIid()
		println "商品title:"+item.getTitle()
		println "商品价格:"+item.getPrice()
		println "卖家信用等级"+item.getSellerCreditScore()
		println "推广点击url:" +item.getClickUrl()
		println	"图片url"+item.getPicUrl()
		println	"30天累计成交量"+item.getCommissionNum()
		println "促销价格"+item.getPromotionPrice()
		item.getItemLocation()
	}
}
